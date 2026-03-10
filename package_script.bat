@echo off
REM Package Python script as executable (Windows)

setlocal enabledelayedexpansion

if "%1"=="" (
    echo Usage: %0 ^<script_path^> [output_name]
    exit /b 1
)

set SCRIPT_PATH=%1
set OUTPUT_NAME=%2
if "%OUTPUT_NAME%"=="" set OUTPUT_NAME=automation_script

echo Packaging %SCRIPT_PATH% as %OUTPUT_NAME%...

REM Install PyInstaller if not present
pip install pyinstaller

REM Create spec file
(
echo # -*- mode: python ; coding: utf-8 -*-
echo a = Analysis(
echo     ['%SCRIPT_PATH%'],
echo     pathex=[],
echo     binaries=[],
echo     datas=[],
echo     hiddenimports=['selenium', 'playwright', 'apscheduler'],
echo     hookspath=[],
echo     hooksconfig={},
echo     runtime_hooks=[],
echo     excludedimports=[],
echo     noarchive=False,
echo )
echo pyz = PYZ(a.pure, a.zipped_data, cipher=None)
echo.
echo exe = EXE(
echo     pyz,
echo     a.scripts,
echo     a.binaries,
echo     a.zipfiles,
echo     a.datas,
echo     [],
echo     name='%OUTPUT_NAME%',
echo     debug=False,
echo     bootloader_ignore_signals=False,
echo     strip=False,
echo     upx=True,
echo     upx_exclude=[],
echo     runtime_tmpdir=None,
echo     console=True,
echo     disable_windowed_traceback=False,
echo     target_arch=None,
echo     codesign_identity=None,
echo     entitlements_file=None,
echo )
) > pyinstaller_temp.spec

REM Build executable
pyinstaller pyinstaller_temp.spec

REM Cleanup
del pyinstaller_temp.spec

echo Packaging completed!
echo Executable: dist\%OUTPUT_NAME%.exe
