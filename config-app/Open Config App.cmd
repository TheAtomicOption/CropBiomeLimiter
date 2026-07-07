@echo off
setlocal
title Crop Biome Limiter Config App
set "APP_DIR=%~dp0"
for %%I in ("%APP_DIR%..\..\..") do set "MINECRAFT_DIR=%%~fI"
set "MODS_DIR=%MINECRAFT_DIR%\mods"
set "MOD_JAR="

for /f "delims=" %%F in ('dir /b /a-d /o-d "%MODS_DIR%\cropbiomelimiter-*.jar" 2^>nul ^| findstr /v /i "-sources.jar"') do if not defined MOD_JAR set "MOD_JAR=%MODS_DIR%\%%F"

if not defined MOD_JAR (
	echo.
	echo Could not find cropbiomelimiter-*.jar in:
	echo %MODS_DIR%
	echo.
	echo Make sure the mod jar is installed in the Minecraft mods folder.
	echo.
	pause
	exit /b 1
)

java -cp "%MOD_JAR%" rocks.theatomicoption.cropbiomelimiter.configapp.ConfigAppServer "%APP_DIR%"
if errorlevel 1 (
	echo.
	echo Crop Biome Limiter Config App could not start.
	echo Check that Java is available, then try again.
	echo.
	pause
)
