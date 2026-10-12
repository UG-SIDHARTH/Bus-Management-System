#!/bin/bash
# ==============================================================================
# Automated Test Runner for Debian Linux
# Tests: Password policy, PBKDF2 crypto, Rate Limiting, Sessions, CSRF, MySQL Driver
# ==============================================================================
set -e

echo "=================================================="
echo " 🚍 Running SwiftBus Automated Test Suite on Debian"
echo " Target Database: MySQL Only"
echo "=================================================="

# Check if compiled test classes exist
if [ ! -f "bin/test/TestSuite.class" ]; then
    echo "Compiling test suite..."
    mkdir -p bin
    javac -cp "bin:lib/mysql-connector-j-26.7.0/mysql-connector-j-26.7.0.jar" -d bin src/test/TestSuite.java
fi

exec java -cp "bin:lib/mysql-connector-j-26.7.0/mysql-connector-j-26.7.0.jar" test.TestSuite "$@"
