@echo off
REM ============================================================================
REM Production Build Script for Mentor Application (Windows)
REM
REM This batch script automates the build process for Windows platform
REM
REM Requirements:
REM - Java 17 or higher (with jpackage tool)
REM - Maven 3.6+ (or use included mvnw.cmd)
REM
REM Usage:
REM   build-production.bat
REM
REM Output:
REM   - Windows installer: installers\windows\Mentor-1.0.0.exe
REM ============================================================================

setlocal enabledelayedexpansion

REM Configuration
set APP_NAME=Mentor
set APP_VERSION=1.0.0
set APP_VENDOR=AlgoMentor
set APP_DESCRIPTION=Student management and teaching platform
set MAIN_CLASS=org.algo.mentor.Launcher
set MAIN_JAR=mentor-1.0.0.jar

set PROJECT_DIR=%~dp0
set BUILD_DIR=%PROJECT_DIR%target
set INSTALLERS_DIR=%PROJECT_DIR%installers
set BUILD_RESOURCES=%PROJECT_DIR%build-resources
set ICONS_DIR=%BUILD_RESOURCES%\icons

echo.
echo ============================================================
echo   Mentor Application - Production Build (Windows)
echo ============================================================
echo.

REM Check Java
echo [1/5] Checking Java installation...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    exit /b 1
)

jpackage --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: jpackage tool not found. Ensure you're using JDK 17+
    exit /b 1
)
echo   [OK] Java and jpackage found

REM Check Maven
echo [2/5] Checking Maven installation...
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    if exist "%PROJECT_DIR%mvnw.cmd" (
        echo   [WARN] Maven not in PATH, using Maven wrapper
        set MVN_CMD=%PROJECT_DIR%mvnw.cmd
    ) else (
        echo ERROR: Maven not found and mvnw.cmd wrapper not available
        exit /b 1
    )
) else (
    set MVN_CMD=mvn
)
echo   [OK] Maven found

REM Clean previous build
echo [3/5] Cleaning previous build...
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
if exist "%INSTALLERS_DIR%" rmdir /s /q "%INSTALLERS_DIR%"
echo   [OK] Build directories cleaned

REM Build with Maven
echo [4/5] Building with Maven...
cd "%PROJECT_DIR%"
call %MVN_CMD% clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Maven build failed
    exit /b 1
)
echo   [OK] Maven build completed

REM Create installer
echo [5/5] Creating Windows installer...

REM Prepare input directory
set INPUT_DIR=%BUILD_DIR%\jpackage-input
if exist "%INPUT_DIR%" rmdir /s /q "%INPUT_DIR%"
mkdir "%INPUT_DIR%"

REM Copy JAR and dependencies
copy "%BUILD_DIR%\%MAIN_JAR%" "%INPUT_DIR%\" >nul
if exist "%BUILD_DIR%\libs" (
    xcopy /s /q "%BUILD_DIR%\libs\*" "%INPUT_DIR%\" >nul
)

REM Create output directory
set OUTPUT_DIR=%INSTALLERS_DIR%\windows
mkdir "%OUTPUT_DIR%"

REM Check for icon
set ICON_FILE=%ICONS_DIR%\windows\icon.ico
set ICON_ARG=
if exist "%ICON_FILE%" (
    set ICON_ARG=--icon "%ICON_FILE%"
    echo   [OK] Using custom icon
) else (
    echo   [WARN] Icon not found, using default
)

REM Run jpackage
jpackage ^
    --type exe ^
    --input "%INPUT_DIR%" ^
    --dest "%OUTPUT_DIR%" ^
    --name "%APP_NAME%" ^
    --main-jar "%MAIN_JAR%" ^
    --main-class "%MAIN_CLASS%" ^
    --app-version "%APP_VERSION%" ^
    --vendor "%APP_VENDOR%" ^
    --description "%APP_DESCRIPTION%" ^
    --copyright "Copyright (C) 2025 %APP_VENDOR%" ^
    --win-dir-chooser ^
    --win-menu ^
    --win-menu-group "%APP_VENDOR%" ^
    --win-shortcut ^
    --win-shortcut-prompt ^
    %ICON_ARG%

if %errorlevel% neq 0 (
    echo ERROR: jpackage failed
    exit /b 1
)

echo.
echo ============================================================
echo   Build Completed Successfully!
echo ============================================================
echo.
echo Installer location:
echo   %OUTPUT_DIR%\%APP_NAME%-%APP_VERSION%.exe
echo.

REM Create build info
set BUILD_INFO=%INSTALLERS_DIR%\BUILD_INFO.txt
echo Mentor Application - Build Information > "%BUILD_INFO%"
echo ======================================== >> "%BUILD_INFO%"
echo. >> "%BUILD_INFO%"
echo Build Date: %DATE% %TIME% >> "%BUILD_INFO%"
echo Application Version: %APP_VERSION% >> "%BUILD_INFO%"
echo Build Platform: Windows >> "%BUILD_INFO%"
echo. >> "%BUILD_INFO%"
echo Installer: >> "%BUILD_INFO%"
echo - Windows: installers\windows\%APP_NAME%-%APP_VERSION%.exe >> "%BUILD_INFO%"

echo Build information saved to: %BUILD_INFO%
echo.
pause
