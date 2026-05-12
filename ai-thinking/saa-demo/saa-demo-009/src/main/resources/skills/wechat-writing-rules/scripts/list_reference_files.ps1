# 无windows环境，脚本未验证 AI基于list_reference_files.sh本扩写的，windows版本需要自行验证
$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$SkillDir = Split-Path -Parent $ScriptDir
$ReferenceDir = Join-Path $SkillDir "references"

if (-not (Test-Path -LiteralPath $ReferenceDir -PathType Container)) {
    [pscustomobject]@{
        status = "missing"
        reference_dir = $ReferenceDir
        references = @()
    } | ConvertTo-Json -Compress
    exit 0
}

$References = Get-ChildItem -LiteralPath $ReferenceDir -File |
    Sort-Object Name |
    ForEach-Object {
        [pscustomobject]@{
            file = $_.Name
            content = Get-Content -LiteralPath $_.FullName -Raw -Encoding UTF8
        }
    }

[pscustomobject]@{
    status = "ok"
    reference_dir = $ReferenceDir
    references = @($References)
} | ConvertTo-Json -Compress
