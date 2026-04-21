param(
    [switch]$SkipInstall
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$frontendDir = Join-Path $repoRoot "frontend"
$backendDir = Join-Path $repoRoot "backend"
$logDir = Join-Path $repoRoot "runtime-logs\local-dev"
$pidFile = Join-Path $logDir "pids.json"
$nodeHome = Join-Path $env:USERPROFILE "tools\node-v14.16.0-win-x64"
$npmCmd = if (Test-Path (Join-Path $nodeHome "npm.cmd")) { Join-Path $nodeHome "npm.cmd" } else { "npm.cmd" }
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"

New-Item -ItemType Directory -Force -Path $logDir | Out-Null

if (-not $SkipInstall) {
    if (-not (Test-Path (Join-Path $frontendDir "node_modules"))) {
        Write-Host "[local-run] frontend/node_modules 缺失，开始安装依赖..."
        & $npmCmd install --scripts-prepend-node-path=true | Out-Host
    }
}

$backendOut = Join-Path $logDir "backend-$timestamp.out.log"
$backendErr = Join-Path $logDir "backend-$timestamp.err.log"
$frontendOut = Join-Path $logDir "frontend-$timestamp.out.log"
$frontendErr = Join-Path $logDir "frontend-$timestamp.err.log"

$backendProc = Start-Process -FilePath "cmd.exe" `
    -ArgumentList "/c", "`"$PSScriptRoot\run-backend-dev.cmd`"" `
    -WorkingDirectory $repoRoot `
    -RedirectStandardOutput $backendOut `
    -RedirectStandardError $backendErr `
    -PassThru

$frontendProc = Start-Process -FilePath "cmd.exe" `
    -ArgumentList "/c", "`"$PSScriptRoot\run-frontend-dev.cmd`"" `
    -WorkingDirectory $repoRoot `
    -RedirectStandardOutput $frontendOut `
    -RedirectStandardError $frontendErr `
    -PassThru

@{
    startedAt = (Get-Date).ToString("s")
    backend = @{
        pid = $backendProc.Id
        url = "http://127.0.0.1:8091"
        stdout = $backendOut
        stderr = $backendErr
    }
    frontend = @{
        pid = $frontendProc.Id
        url = "http://127.0.0.1:6618"
        stdout = $frontendOut
        stderr = $frontendErr
    }
} | ConvertTo-Json -Depth 4 | Set-Content -Path $pidFile -Encoding UTF8

Write-Host "[local-run] backend pid=$($backendProc.Id) url=http://127.0.0.1:8091"
Write-Host "[local-run] frontend pid=$($frontendProc.Id) url=http://127.0.0.1:6618"
Write-Host "[local-run] pid file: $pidFile"
