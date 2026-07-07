@echo off
setlocal EnableExtensions EnableDelayedExpansion
title Crop Biome Limiter Config App
for %%I in ("%~dp0.") do set "APP_DIR=%%~fI"
for %%I in ("%APP_DIR%\..\..\..") do set "MINECRAFT_DIR=%%~fI"
for %%I in ("%MINECRAFT_DIR%\..") do set "REPO_DIR=%%~fI"
set "MODS_DIR=%MINECRAFT_DIR%\mods"
set "MOD_JAR="
set "CONFIG_APP_CLASSPATH="

call :find_mod_jar "%MODS_DIR%"

if not defined MOD_JAR if exist "%REPO_DIR%\build.gradle" call :find_mod_jar "%REPO_DIR%\build\libs"

if defined MOD_JAR (
	set "CONFIG_APP_CLASSPATH=%MOD_JAR%"
) else if exist "%REPO_DIR%\build\classes\java\main\rocks\theatomicoption\cropbiomelimiter\configapp\ConfigAppServer.class" (
	set "CONFIG_APP_CLASSPATH=%REPO_DIR%\build\classes\java\main;%REPO_DIR%\build\resources\main"
)

if not defined CONFIG_APP_CLASSPATH (
	echo.
	echo Could not find the Crop Biome Limiter config app helper.
	echo Looked for an installed mod jar in:
	echo %MODS_DIR%
	if exist "%REPO_DIR%\build.gradle" (
		echo.
		echo Also looked for dev build output in:
		echo %REPO_DIR%\build\libs
		echo %REPO_DIR%\build\classes\java\main
	)
	echo.
	echo Build or install the mod jar, then try again.
	echo.
	pause
	exit /b 1
)

java -cp "%CONFIG_APP_CLASSPATH%" rocks.theatomicoption.cropbiomelimiter.configapp.ConfigAppServer "%APP_DIR%"
if errorlevel 1 (
	echo.
	echo Crop Biome Limiter Config App could not start.
	echo Check that Java is available, then try again.
	echo.
	pause
)

exit /b %errorlevel%

:find_mod_jar
set "SEARCH_DIR=%~1"
if not exist "%SEARCH_DIR%" exit /b 0
for /f "delims=" %%F in ('dir /b /a-d /o-d "%SEARCH_DIR%\cropbiomelimiter-*.jar" 2^>nul') do (
	set "FILE_NAME=%%F"
	if /I "!FILE_NAME:-sources.jar=!"=="!FILE_NAME!" if not defined MOD_JAR set "MOD_JAR=%SEARCH_DIR%\%%F"
)
exit /b 0
