$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

docker compose -p pvms up --build -d

Write-Host "[docker-up] frontend: http://127.0.0.1:6618/pvms/"
Write-Host "[docker-up] backend: http://127.0.0.1:8091"
Write-Host "[docker-up] backend health: http://127.0.0.1:8091/api/system/health"
