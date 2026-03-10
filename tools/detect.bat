@echo off
chcp 65001 >nul
echo 正在检测浏览器版本...
echo.
powershell -ExecutionPolicy Bypass -File "%~dp0detect_and_download.ps1" %*
