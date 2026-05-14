#!/usr/bin/env bash

set -euo pipefail

usage() {
    cat <<'EOF'
Usage:
  scripts/copy-module.sh <source-module> <target-module>

Example:
  scripts/copy-module.sh saa-demo-001 saa-demo-002
EOF
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    usage
    exit 0
fi

if [[ $# -ne 2 ]]; then
    usage
    exit 1
fi

source_module="$1"
target_module="$2"
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
root_dir="$(cd "${script_dir}/.." && pwd)"
source_dir="${root_dir}/${source_module}"
target_dir="${root_dir}/${target_module}"
root_pom="${root_dir}/pom.xml"

if [[ ! -d "${source_dir}" ]]; then
    echo "Source module does not exist: ${source_module}" >&2
    exit 1
fi

if [[ ! -f "${source_dir}/pom.xml" ]]; then
    echo "Source module is missing pom.xml: ${source_module}" >&2
    exit 1
fi

if [[ -e "${target_dir}" ]]; then
    echo "Target module already exists: ${target_module}" >&2
    exit 1
fi

copy_module() {
    cp -R "${source_dir}" "${target_dir}"

    if [[ -d "${target_dir}/target" ]]; then
        rm -rf "${target_dir}/target"
    fi
}

rename_paths() {
    find "${target_dir}" -depth -name "*${source_module}*" -print0 | while IFS= read -r -d '' path; do
        new_path="${path//${source_module}/${target_module}}"
        mv "${path}" "${new_path}"
    done
}

replace_module_name_in_files() {
    local finder=(grep -rlZ --binary-files=without-match --exclude-dir=.git --exclude-dir=target -- "${source_module}" "${target_dir}")

    if command -v rg >/dev/null 2>&1; then
        finder=(rg -l -0 --hidden --glob '!**/.git/**' --glob '!**/target/**' --fixed-strings "${source_module}" "${target_dir}")
    fi

    "${finder[@]}" | while IFS= read -r -d '' file; do
        SOURCE_MODULE="${source_module}" TARGET_MODULE="${target_module}" \
            perl -0pi -e 's/\Q$ENV{SOURCE_MODULE}\E/$ENV{TARGET_MODULE}/g' "${file}"
    done
}

rewrite_target_pom_artifact_id() {
    local target_pom="${target_dir}/pom.xml"

    TARGET_MODULE="${target_module}" perl -0pi -e '
        s{(<project\b[^>]*>.*?(?:<parent>.*?</parent>\s*)?(?:<groupId>.*?</groupId>\s*)?<artifactId>)[^<]+(</artifactId>)}
         {$1 . $ENV{TARGET_MODULE} . $2}se
            or die "Failed to update project artifactId in ${ARGV}\n";
    ' "${target_pom}"
}

sync_root_modules() {
    if [[ ! -f "${root_pom}" ]]; then
        return
    fi

    local modules=()
    local module_dir
    while IFS= read -r module_dir; do
        modules+=("$(basename "${module_dir}")")
    done < <(find "${root_dir}" -mindepth 1 -maxdepth 1 -type d -exec test -f "{}/pom.xml" ';' -print | sort)

    if [[ ${#modules[@]} -eq 0 ]]; then
        return
    fi

    local modules_block
    modules_block=$(
        printf '    <modules>\n'
        local module
        for module in "${modules[@]}"; do
            printf '        <module>%s</module>\n' "${module}"
        done
        printf '    </modules>\n'
    )

    local temp_file
    temp_file="$(mktemp)"

    if grep -q "<modules>" "${root_pom}"; then
        MODULES_BLOCK="${modules_block}" awk '
            BEGIN {
                in_modules = 0
                inserted = 0
                split(ENVIRON["MODULES_BLOCK"], lines, "\n")
            }
            /<modules>/ {
                in_modules = 1
                if (!inserted) {
                    for (i = 1; i <= length(lines); i++) {
                        if (lines[i] != "") {
                            print lines[i]
                        }
                    }
                    inserted = 1
                }
                next
            }
            /<\/modules>/ {
                in_modules = 0
                next
            }
            !in_modules { print }
        ' "${root_pom}" > "${temp_file}"
    else
        MODULES_BLOCK="${modules_block}" awk '
            BEGIN {
                inserted = 0
                split(ENVIRON["MODULES_BLOCK"], lines, "\n")
            }
            /<\/project>/ && !inserted {
                for (i = 1; i <= length(lines); i++) {
                    if (lines[i] != "") {
                        print lines[i]
                    }
                }
                inserted = 1
            }
            { print }
        ' "${root_pom}" > "${temp_file}"
    fi

    mv "${temp_file}" "${root_pom}"
}

copy_module
rename_paths
replace_module_name_in_files
rewrite_target_pom_artifact_id
sync_root_modules

echo "Module copied successfully:"
echo "  from: ${source_module}"
echo "  to:   ${target_module}"
