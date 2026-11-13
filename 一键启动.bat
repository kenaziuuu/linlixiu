@echo off
cd /d "%~dp0"
title 邻里修 - 一键启动

where powershell >nul 2>nul
if errorlevel 1 (
    echo.
    echo  [错误] 没有找到 PowerShell，无法自动启动
    echo         请手动运行 tools\launcher.ps1
    echo.
    pause
    exit /b 1
)

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\launcher.ps1"

if errorlevel 1 pause
