@echo off
setlocal
cd /d "%~dp0"
title Linlixiu - Stopper

where powershell.exe >nul 2>nul
if errorlevel 1 (
    echo.
    echo [ERROR] Windows PowerShell was not found.
    echo Please run tools\stopper.ps1 with PowerShell.
    echo.
    pause
    exit /b 1
)

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\stopper.ps1"
set "LAUNCH_EXIT=%ERRORLEVEL%"

if not "%LAUNCH_EXIT%"=="0" (
    echo.
    echo [ERROR] Stopper exited with code %LAUNCH_EXIT%.
    echo Review the error above before closing this window.
    echo.
    pause
)

exit /b %LAUNCH_EXIT%
