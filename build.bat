@echo off
REM Build script for Auto Operation Platform (Windows)

setlocal enabledelayedexpansion

echo Building Auto Operation Platform...

REM Build backend
echo Building backend...
call gradlew.bat clean build -x test
if errorlevel 1 (
    echo Backend build failed
    exit /b 1
)

REM Build frontend
echo Building frontend...
cd frontend
call npm install
call npm run build
cd ..

echo Build completed successfully!
echo Backend JAR: build\libs\auto-operation-0.0.1-SNAPSHOT.jar
echo Frontend dist: frontend\dist\
