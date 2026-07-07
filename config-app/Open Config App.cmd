@echo off
setlocal
title Crop Biome Limiter Config App
set "APP_DIR=%~dp0"

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%APP_DIR%open-config-app.ps1"
if errorlevel 1 (
	echo.
	echo Crop Biome Limiter Config App could not start.
	echo Check that Windows PowerShell is available, then try again.
	echo.
	pause
)
