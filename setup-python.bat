@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

echo ============================================
echo   Python Embedded Environment Setup
echo ============================================
echo.

set "PYTHON_VERSION=3.12.8"
set "PYTHON_DIR=python"
set "PYTHON_EMBED_DIR=%PYTHON_DIR%\python-3.12.8-embed-amd64"
set "PYTHON_EXE=%PYTHON_EMBED_DIR%\python.exe"
set "PYTHON_ZIP=%PYTHON_DIR%\python-%PYTHON_VERSION%-embed-amd64.zip"
set "PYTHON_URL=https://www.python.org/ftp/python/%PYTHON_VERSION%/python-%PYTHON_VERSION%-embed-amd64.zip"
set "GET_PIP_URL=https://bootstrap.pypa.io/get-pip.py"
set "PTH_FILE=%PYTHON_EMBED_DIR%\python312._pth"
set "DRIVERS_DIR=%PYTHON_DIR%\drivers"
set "EDGE_DRIVER=%DRIVERS_DIR%\msedgedriver.exe"
set "MIRROR_BASE=https://registry.npmmirror.com/-/binary/edgedriver"

REM Step 1
echo [1/6] Creating directories...
if not exist "%PYTHON_DIR%" mkdir "%PYTHON_DIR%"
if not exist "%DRIVERS_DIR%" mkdir "%DRIVERS_DIR%"
echo       Done.

REM Step 2
if exist "%PYTHON_EXE%" (
    echo [2/6] Python already exists, skipping download.
) else (
    echo [2/6] Downloading Python %PYTHON_VERSION% embeddable package...

    if not exist "%PYTHON_ZIP%" (
        curl.exe -L -o "%PYTHON_ZIP%" "%PYTHON_URL%"
        if errorlevel 1 (
            echo [ERROR] Download failed. Check your network connection.
            exit /b 1
        )
        echo       Download complete.
    ) else (
        echo       ZIP already exists, skipping download.
    )

    echo       Extracting...
    if not exist "%PYTHON_EMBED_DIR%" mkdir "%PYTHON_EMBED_DIR%"
    powershell -NoProfile -Command "Expand-Archive -Path '%PYTHON_ZIP%' -DestinationPath '%PYTHON_EMBED_DIR%' -Force"
    if errorlevel 1 (
        echo [ERROR] Extraction failed.
        exit /b 1
    )
    echo       Extraction complete.
)

REM Step 3
echo [3/6] Configuring python312._pth ...
if not exist "%PYTHON_EMBED_DIR%\Lib\site-packages" mkdir "%PYTHON_EMBED_DIR%\Lib\site-packages"

> "%PTH_FILE%" (
    echo python312.zip
    echo .
    echo Lib\site-packages
    echo import site
)
echo       Done.

REM Step 4
"%PYTHON_EXE%" -m pip --version >nul 2>&1
if %errorlevel% equ 0 (
    echo [4/6] pip already installed, skipping.
) else (
    echo [4/6] Installing pip...

    if not exist "%PYTHON_DIR%\get-pip.py" (
        curl.exe -L -o "%PYTHON_DIR%\get-pip.py" "%GET_PIP_URL%"
        if errorlevel 1 (
            echo [ERROR] Failed to download get-pip.py
            exit /b 1
        )
    )

    "%PYTHON_EXE%" "%PYTHON_DIR%\get-pip.py" --no-warn-script-location
    if errorlevel 1 (
        echo [ERROR] pip installation failed.
        exit /b 1
    )
    echo       pip installed.
)

REM Step 5
echo [5/6] Installing Python dependencies...
if exist "scripts\requirements.txt" (
    "%PYTHON_EXE%" -m pip install -r scripts\requirements.txt --no-warn-script-location
    if errorlevel 1 (
        echo [WARN] Some dependencies failed to install.
    ) else (
        echo       Dependencies installed.
    )
) else (
    echo [WARN] scripts\requirements.txt not found, skipping.
)

REM Step 6: Download Edge WebDriver from npmmirror
if exist "%EDGE_DRIVER%" (
    echo [6/6] Edge WebDriver already exists, skipping.
) else (
    echo [6/6] Downloading Edge WebDriver from npmmirror...

    set "EDGE_VERSION="
    for /f "tokens=*" %%D in ('dir /b /ad "C:\Program Files (x86)\Microsoft\Edge\Application\" 2^>nul ^| findstr /R "^[0-9]"') do set "EDGE_VERSION=%%D"
    if "!EDGE_VERSION!"=="" (
        for /f "tokens=*" %%D in ('dir /b /ad "C:\Program Files\Microsoft\Edge\Application\" 2^>nul ^| findstr /R "^[0-9]"') do set "EDGE_VERSION=%%D"
    )

    if "!EDGE_VERSION!"=="" (
        echo [WARN] Edge browser not found, skipping driver download.
        goto :setup_done
    )

    echo       Detected Edge version: !EDGE_VERSION!
    set "DRIVER_URL=%MIRROR_BASE%/!EDGE_VERSION!/edgedriver_win64.zip"
    set "DRIVER_ZIP=%DRIVERS_DIR%\edgedriver_win64.zip"

    echo       Downloading from npmmirror...
    curl.exe -L -o "!DRIVER_ZIP!" "!DRIVER_URL!"
    if errorlevel 1 (
        echo [WARN] Driver download failed. You can download manually later.
        goto :setup_done
    )

    echo       Extracting driver...
    powershell -NoProfile -Command "Expand-Archive -Path '!DRIVER_ZIP!' -DestinationPath '%DRIVERS_DIR%' -Force"
    if errorlevel 1 (
        echo [WARN] Driver extraction failed.
        goto :setup_done
    )

    if exist "%EDGE_DRIVER%" (
        echo       Edge WebDriver installed: %EDGE_DRIVER%
    ) else (
        echo [WARN] msedgedriver.exe not found after extraction.
    )

    del /q "!DRIVER_ZIP!" >nul 2>&1
)

:setup_done
echo.
echo ============================================
echo   Setup complete!
echo ============================================
echo.
echo   Python path: %PYTHON_EXE%
"%PYTHON_EXE%" --version
if exist "%EDGE_DRIVER%" (
    echo   Edge driver:  %EDGE_DRIVER%
)
echo.
echo   You can now start the Spring Boot application.
echo ============================================

endlocal
