#!/bin/bash
# Build script for Auto Operation Platform

set -e

echo "Building Auto Operation Platform..."

# Build backend
echo "Building backend..."
cd "$(dirname "$0")"
./gradlew clean build -x test

# Build frontend
echo "Building frontend..."
cd frontend
npm install
npm run build
cd ..

echo "Build completed successfully!"
echo "Backend JAR: build/libs/auto-operation-0.0.1-SNAPSHOT.jar"
echo "Frontend dist: frontend/dist/"
