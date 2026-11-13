@echo off
setlocal
cd /d "%~dp0"
title Linlixiu - Launcher

where powershell.exe >nul 2>nul
if errorlevel 1 (
    echo.
    echo [ERROR] Windows PowerShell was not found.
    echo Please run tools\launcher.ps1 with PowerShell.
    echo.
    pause
    exit /b 1
)

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\launcher.ps1"
set "LAUNCH_EXIT=%ERRORLEVEL%"

if not "%LAUNCH_EXIT%"=="0" (
    echo.
    echo [ERROR] Launcher exited with code %LAUNCH_EXIT%.
    echo Review the error above before closing this window.
    echo.
    pause
)

exit /b %LAUNCH_EXIT%
