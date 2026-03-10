#!/bin/bash
# Package Python script as executable

if [ $# -lt 1 ]; then
    echo "Usage: $0 <script_path> [output_name]"
    exit 1
fi

SCRIPT_PATH=$1
OUTPUT_NAME=${2:-automation_script}

echo "Packaging $SCRIPT_PATH as $OUTPUT_NAME..."

# Install PyInstaller if not present
pip install pyinstaller

# Create spec file
cat > pyinstaller_temp.spec <<EOF
# -*- mode: python ; coding: utf-8 -*-
a = Analysis(
    ['$SCRIPT_PATH'],
    pathex=[],
    binaries=[],
    datas=[],
    hiddenimports=['selenium', 'playwright', 'apscheduler'],
    hookspath=[],
    hooksconfig={},
    runtime_hooks=[],
    excludedimports=[],
    noarchive=False,
)
pyz = PYZ(a.pure, a.zipped_data, cipher=None)

exe = EXE(
    pyz,
    a.scripts,
    a.binaries,
    a.zipfiles,
    a.datas,
    [],
    name='$OUTPUT_NAME',
    debug=False,
    bootloader_ignore_signals=False,
    strip=False,
    upx=True,
    upx_exclude=[],
    runtime_tmpdir=None,
    console=True,
    disable_windowed_traceback=False,
    target_arch=None,
    codesign_identity=None,
    entitlements_file=None,
)
EOF

# Build executable
pyinstaller pyinstaller_temp.spec

# Cleanup
rm pyinstaller_temp.spec

echo "Packaging completed!"
echo "Executable: dist/$OUTPUT_NAME"
