#!/bin/sh

set -eu

SCRIPT_DIR=$(CDPATH= cd "$(dirname "$0")" && pwd)
SKILL_DIR=$(CDPATH= cd "$SCRIPT_DIR/.." && pwd)
REFERENCE_DIR="$SKILL_DIR/references"

if [ ! -d "$REFERENCE_DIR" ]; then
  printf '{"status":"missing","reference_dir":"%s","references":[]}\n' "$REFERENCE_DIR"
  exit 0
fi

json_escape_string() {
  printf '%s' "$1" | sed 's/\\/\\\\/g; s/"/\\"/g'
}

json_escape_file() {
  awk '
    {
      gsub(/\\/, "\\\\")
      gsub(/"/, "\\\"")
      gsub(/\t/, "\\t")
      gsub(/\r/, "\\r")
      if (NR > 1) {
        printf "\\n"
      }
      printf "%s", $0
    }
  ' "$1"
}

printf '{"status":"ok","reference_dir":"%s","references":[' "$(json_escape_string "$REFERENCE_DIR")"

first=true
for file in "$REFERENCE_DIR"/*; do
  [ -f "$file" ] && printf '%s\n' "$file"
done | sort | while IFS= read -r file; do
  if [ "$first" = true ]; then
    first=false
  else
    printf ','
  fi
  name=$(basename "$file")
  printf '{"file":"%s","content":"' "$(json_escape_string "$name")"
  json_escape_file "$file"
  printf '"}'
done

printf ']}\n'
