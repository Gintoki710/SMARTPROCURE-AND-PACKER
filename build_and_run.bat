@echo off
title Smart Procurement Budgeter & Warehouse Box Packer
echo ====================================================================
echo  SMART PROCUREMENT BUDGETER & WAREHOUSE BOX PACKER
echo  DSA Laboratory Capstone Suite (Java 26 + Pure Web Frontend)
echo ====================================================================
echo.

if not exist "bin" mkdir bin

echo [1/2] Compiling Java source files...
dir /s /b src\*.java > sources.txt
javac -d bin @sources.txt
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed.
    del sources.txt
    pause
    exit /b %errorlevel%
)
del sources.txt
echo [2/2] Compilation successful!
echo.
echo Starting Embedded HTTP Server on http://localhost:8080 ...
echo Press Ctrl+C in this window to stop the server.
echo.
start http://localhost:8080
java -cp bin server.HttpServerApp 8080
pause

