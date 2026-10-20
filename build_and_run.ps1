# Smart Procurement Budgeter & Warehouse Box Packer
# PowerShell Launcher Script

Write-Host "====================================================================" -ForegroundColor Cyan
Write-Host " SMART PROCUREMENT BUDGETER & WAREHOUSE BOX PACKER" -ForegroundColor Cyan
Write-Host " DSA Capstone Suite (Java Native + Classy Web UI)" -ForegroundColor Cyan
Write-Host "====================================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "[1/2] Compiling Java classes..." -ForegroundColor Yellow
$javaFiles = (Get-ChildItem -Path "src" -Recurse -Filter "*.java").FullName
javac -d bin $javaFiles

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Java compilation failed!" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "[2/2] Compilation succeeded!" -ForegroundColor Green
Write-Host "Starting Embedded Server on http://localhost:8080 ..." -ForegroundColor Cyan

Start-Process "http://localhost:8080"
java -cp bin server.HttpServerApp 8080

