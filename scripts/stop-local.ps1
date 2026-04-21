$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$pidFile = Join-Path $repoRoot "runtime-logs\local-dev\pids.json"

if (-not (Test-Path $pidFile)) {
    Write-Host "[local-stop] 未找到 pid 文件: $pidFile"
    exit 0
}

$payload = Get-Content $pidFile -Raw | ConvertFrom-Json
$pids = @($payload.backend.pid, $payload.frontend.pid) | Where-Object { $_ }

foreach ($pid in $pids) {
    $process = Get-Process -Id $pid -ErrorAction SilentlyContinue
    if ($process) {
        Stop-Process -Id $pid -Force
        Write-Host "[local-stop] 已停止 pid=$pid"
    }
}
