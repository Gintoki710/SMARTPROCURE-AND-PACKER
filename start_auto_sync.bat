@echo off
title DSA Project - GitHub Auto Sync
echo Starting Automatic Git Synchronization with GitHub...
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0auto_git_sync.ps1"
pause

