#!/bin/bash
# ==============================================================================
# Build Script for Debian Linux (Debian 11 / 12 / Ubuntu)
# ==============================================================================
set -e

echo "=================================================="
echo " 🚍 Building SwiftBus on Debian Linux"
echo "=================================================="

# Check for javac
if ! command -v javac &> /dev/null; then
    echo "Error: javac not found."
    echo "Install OpenJDK 21 on Debian with:"
    echo "  sudo apt update && sudo apt install -y openjdk-21-jdk"
    exit 1
fi

JAVA_VER=$(javac -version 2>&1)
echo "Found Java Compiler: ${JAVA_VER}"

mkdir -p bin
find src -name "*.java" > sources.txt

# Compile using Linux classpath separator (:)
javac -cp "lib/mysql-connector-j-26.7.0/mysql-connector-j-26.7.0.jar" -d bin @sources.txt
rm -f sources.txt

echo "=================================================="
echo " ✅ Build Successful! Output directory: ./bin"
echo " To run natively, execute: ./run-debian.sh"
echo "=================================================="
