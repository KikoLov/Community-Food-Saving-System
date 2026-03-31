@echo off
setlocal

cd /d "%~dp0"
echo [Food Saving Platform] starting...
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File ".\scripts\start-all.ps1"

echo.
echo Script finished. You can close this window.
pause
