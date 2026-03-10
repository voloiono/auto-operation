@echo off
chcp 65001 >nul
echo ========================================
echo    浏览器版本检测工具
echo ========================================
echo.

echo [Edge 浏览器]
for %%p in (
    "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
    "C:\Program Files\Microsoft\Edge\Application\msedge.exe"
) do (
    if exist %%p (
        for /f "tokens=*" %%v in ('powershell -command "(Get-Item %%p).VersionInfo.FileVersion"') do (
            echo   路径: %%~p
            echo   版本: %%v
            for /f "tokens=1 delims=." %%m in ("%%v") do echo   主版本: %%m
        )
        goto :edge_done
    )
)
echo   未安装
:edge_done
echo.

echo [Chrome 浏览器]
for %%p in (
    "C:\Program Files\Google\Chrome\Application\chrome.exe"
    "C:\Program Files (x86)\Google\Chrome\Application\chrome.exe"
) do (
    if exist %%p (
        for /f "tokens=*" %%v in ('powershell -command "(Get-Item %%p).VersionInfo.FileVersion"') do (
            echo   路径: %%~p
            echo   版本: %%v
            for /f "tokens=1 delims=." %%m in ("%%v") do echo   主版本: %%m
        )
        goto :chrome_done
    )
)
echo   未安装
:chrome_done
echo.

echo [Firefox 浏览器]
for %%p in (
    "C:\Program Files\Mozilla Firefox\firefox.exe"
    "C:\Program Files (x86)\Mozilla Firefox\firefox.exe"
) do (
    if exist %%p (
        for /f "tokens=*" %%v in ('powershell -command "(Get-Item %%p).VersionInfo.FileVersion"') do (
            echo   路径: %%~p
            echo   版本: %%v
            for /f "tokens=1 delims=." %%m in ("%%v") do echo   主版本: %%m
        )
        goto :firefox_done
    )
)
echo   未安装
:firefox_done
echo.

echo [Internet Explorer]
for %%p in (
    "C:\Program Files\Internet Explorer\iexplore.exe"
    "C:\Program Files (x86)\Internet Explorer\iexplore.exe"
) do (
    if exist %%p (
        for /f "tokens=*" %%v in ('powershell -command "(Get-Item %%p).VersionInfo.FileVersion"') do (
            echo   路径: %%~p
            echo   版本: %%v
            for /f "tokens=1 delims=." %%m in ("%%v") do echo   主版本: %%m
        )
        goto :ie_done
    )
)
echo   未安装
:ie_done
echo.

echo ========================================
echo    驱动下载地址
echo ========================================
echo.
echo [Edge 驱动]
echo   https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/
echo   直接下载: https://msedgedriver.azureedge.net/[版本号]/edgedriver_win64.zip
echo.
echo [Chrome 驱动]
echo   https://googlechromelabs.github.io/chrome-for-testing/
echo.
echo [Firefox 驱动 (geckodriver)]
echo   https://github.com/mozilla/geckodriver/releases
echo.
echo [IE 驱动]
echo   https://www.selenium.dev/downloads/
echo.
echo ========================================
pause
