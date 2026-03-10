@echo off
REM Installation script for Auto Operation Platform on Windows

setlocal enabledelayedexpansion

echo Auto Operation Platform - Installation Script
echo =============================================

REM Check prerequisites
echo Checking prerequisites...

where java >nul 2>nul
if errorlevel 1 (
    echo Error: Java 25 is not installed
    exit /b 1
)

where python >nul 2>nul
if errorlevel 1 (
    echo Error: Python 3 is not installed
    exit /b 1
)

where npm >nul 2>nul
if errorlevel 1 (
    echo Error: Node.js is not installed
    exit /b 1
)

echo Prerequisites check passed!

REM Create directories
echo Creating directories...
if not exist "scripts\generated" mkdir scripts\generated
if not exist "logs" mkdir logs

REM Install Python dependencies
echo Installing Python dependencies...
pip install -r scripts\requirements.txt

REM Build the project
echo Building project...
call build.bat

echo.
echo Installation completed successfully!
echo.
echo To start the application:
echo   Backend:  java -jar build\libs\auto-operation-0.0.1-SNAPSHOT.jar
echo   Frontend: cd frontend ^&^& npm run dev
echo.
echo Access the application at:
echo   Frontend: http://localhost:5173
echo   Backend:  http://localhost:8080
