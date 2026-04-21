param(
    [string]$Remote = "origin"
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot

Push-Location $repoRoot
try {
    $branch = (git branch --show-current).Trim()
    if (-not $branch) {
        throw "无法识别当前分支，请确认当前目录是 Git 工作区。"
    }

    Write-Host "[git-push] repository: $repoRoot"
    Write-Host "[git-push] branch: $branch"
    Write-Host "[git-push] remote: $Remote"

    git rev-parse --abbrev-ref --symbolic-full-name "@{u}" *> $null
    if ($LASTEXITCODE -eq 0) {
        git push
    } else {
        git push --set-upstream $Remote $branch
    }

    if ($LASTEXITCODE -ne 0) {
        throw "git push 执行失败。"
    }
}
finally {
    Pop-Location
}
