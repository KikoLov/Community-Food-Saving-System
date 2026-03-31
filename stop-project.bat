@echo off
setlocal

cd /d "%~dp0"
echo [Food Saving Platform] stopping...
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\stop-all.ps1"

echo.
echo Script finished. You can close this window.
pause
