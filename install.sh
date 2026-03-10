#!/bin/bash
# Installation script for Auto Operation Platform on Linux/Mac

set -e

echo "Auto Operation Platform - Installation Script"
echo "=============================================="

# Check prerequisites
echo "Checking prerequisites..."

if ! command -v java &> /dev/null; then
    echo "Error: Java 25 is not installed"
    exit 1
fi

if ! command -v python3 &> /dev/null; then
    echo "Error: Python 3 is not installed"
    exit 1
fi

if ! command -v npm &> /dev/null; then
    echo "Error: Node.js is not installed"
    exit 1
fi

echo "Prerequisites check passed!"

# Create directories
echo "Creating directories..."
mkdir -p scripts/generated
mkdir -p logs

# Install Python dependencies
echo "Installing Python dependencies..."
pip3 install -r scripts/requirements.txt

# Build the project
echo "Building project..."
./build.sh

# Create systemd service (Linux only)
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    echo "Creating systemd service..."
    sudo tee /etc/systemd/system/auto-operation.service > /dev/null <<EOF
[Unit]
Description=Auto Operation Platform
After=network.target

[Service]
Type=simple
User=$USER
WorkingDirectory=$(pwd)
ExecStart=java -jar build/libs/auto-operation-0.0.1-SNAPSHOT.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF
    sudo systemctl daemon-reload
    echo "Service installed. Start with: sudo systemctl start auto-operation"
fi

echo ""
echo "Installation completed successfully!"
echo ""
echo "To start the application:"
echo "  Backend:  java -jar build/libs/auto-operation-0.0.1-SNAPSHOT.jar"
echo "  Frontend: cd frontend && npm run dev"
echo ""
echo "Access the application at:"
echo "  Frontend: http://localhost:5173"
echo "  Backend:  http://localhost:8080"
