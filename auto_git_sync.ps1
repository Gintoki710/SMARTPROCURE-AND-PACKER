# ==========================================================
# Auto Git Sync - Pack & Plan
# Automatically commits and pushes local code changes to GitHub,
# and periodically checks for remote changes.
# ==========================================================

param (
    [int]$IntervalSeconds = 5,
    [string]$Branch = "main"
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $scriptDir

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   DSA PROJECT - AUTOMATIC GITHUB TWO-WAY SYNC WATCHER   " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Watching directory: $scriptDir" -ForegroundColor Gray
Write-Host "Target Remote: origin/$Branch" -ForegroundColor Gray
Write-Host "Polling Interval: $IntervalSeconds seconds" -ForegroundColor Gray
Write-Host "Press Ctrl+C to stop auto-sync.`n" -ForegroundColor Yellow

$lastRemoteCheck = [DateTime]::MinValue

while ($true) {
    try {
        # Check every 60 seconds for remote incoming changes
        if ((Get-Date) - $lastRemoteCheck -gt [TimeSpan]::FromSeconds(60)) {
            $lastRemoteCheck = Get-Date
            $fetchOutput = git fetch origin $Branch 2>&1
            $behindCount = (git rev-list --count HEAD..origin/$Branch 2>$null)
            if ($behindCount -and [int]$behindCount -gt 0) {
                Write-Host "[$(Get-Date -Format 'HH:mm:ss')] Remote changes detected ($behindCount commits behind). Pulling..." -ForegroundColor Magenta
                git pull --rebase origin $Branch
                Write-Host "[$(Get-Date -Format 'HH:mm:ss')] Successfully synced remote changes into local files!" -ForegroundColor Green
            }
        }

        # Check local uncommitted changes
        $status = git status --porcelain
        if ($status) {
            Write-Host "[$(Get-Date -Format 'HH:mm:ss')] Local file change detected. Waiting 3s for write completion..." -ForegroundColor DarkYellow
            Start-Sleep -Seconds 3

            # Stage all changes
            git add -A

            # Get list of changed file basenames
            $changedFiles = (git diff --cached --name-only)
            if ($changedFiles) {
                $fileList = ($changedFiles | ForEach-Object { Split-Path $_ -Leaf }) -join ", "
                if ($fileList.Length -gt 45) { $fileList = $fileList.Substring(0, 42) + "..." }
                $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
                $commitMsg = "auto: update $fileList ($timestamp)"

                Write-Host "[$(Get-Date -Format 'HH:mm:ss')] Committing: $commitMsg" -ForegroundColor Cyan
                git commit -m $commitMsg

                Write-Host "[$(Get-Date -Format 'HH:mm:ss')] Pushing to GitHub (origin/$Branch)..." -ForegroundColor Yellow
                $pushRes = git push origin $Branch 2>&1
                if ($LASTEXITCODE -eq 0) {
                    Write-Host "[$(Get-Date -Format 'HH:mm:ss')] Successfully pushed to GitHub!" -ForegroundColor Green
                } else {
                    Write-Host "[$(Get-Date -Format 'HH:mm:ss')] Push warning/conflict, attempting rebase pull first..." -ForegroundColor Red
                    git pull --rebase origin $Branch
                    git push origin $Branch
                }
            }
        }
    } catch {
        Write-Host "[$(Get-Date -Format 'HH:mm:ss')] Sync error: $_" -ForegroundColor Red
    }

    Start-Sleep -Seconds $IntervalSeconds
}
