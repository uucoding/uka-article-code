#!/usr/bin/env bash

set -euo pipefail

REFERENCE_DIR="src/main/resources/skills/wechat-writing-rules/references"

if [[ ! -d "$REFERENCE_DIR" ]]; then
  printf '{"status":"missing","reference_dir":"%s","files":[]}\n' "$REFERENCE_DIR"
  exit 0
fi

printf '{"status":"ok","reference_dir":"%s","files":[' "$REFERENCE_DIR"

first=true
while IFS= read -r file; do
  name="$(basename "$file")"
  if [[ "$first" == true ]]; then
    first=false
  else
    printf ','
  fi
  printf '"%s"' "$name"
done < <(find "$REFERENCE_DIR" -maxdepth 1 -type f | sort)

printf ']}\n'
