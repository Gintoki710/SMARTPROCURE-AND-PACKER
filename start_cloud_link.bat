@echo off
title DSA Project - Cloud Edge Public Tunnel
echo ====================================================================
echo  STARTING CLOUD EDGE PUBLIC HTTPS TUNNEL (Cloudflare Global Edge)
echo ====================================================================
echo.
echo Make sure the local server is running on port 8080 first!
echo Requesting secure public HTTPS URL...
echo.
"C:\Program Files (x86)\cloudflared\cloudflared.exe" tunnel --url http://localhost:8080 --no-autoupdate
pause
