#!/bin/bash
# ==============================================================================
# Run Script for Debian Linux (Pentium / 4GB RAM Tuning)
# ==============================================================================
set -e

PORT=${PORT:-6969}
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-3307}
DB_NAME=${DB_NAME:-bus_reservation}
DB_USER=${DB_USER:-root}
DB_PASSWORD=${DB_PASSWORD:-root@6069}

export PORT DB_HOST DB_PORT DB_NAME DB_USER DB_PASSWORD

echo "=================================================="
echo " 🚍 Launching SwiftBus on Debian Linux"
echo " Web Portal URL: http://localhost:${PORT}"
echo " Database Target: ${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo " JVM Settings: -Xms128m -Xmx384m -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"
echo "=================================================="

# Check if compiled classes exist
if [ ! -d "bin/main" ] || [ ! -f "bin/main/Main.class" ]; then
    echo "Compiled classes not found. Running ./build-debian.sh..."
    ./build-debian.sh
fi

# Execute with optimized low-spec flags and Linux classpath separator (:)
exec java \
  -Xms128m \
  -Xmx384m \
  -XX:+UseSerialGC \
  -XX:TieredStopAtLevel=1 \
  -Xss512k \
  -cp "bin:lib/mysql-connector-j-26.7.0/mysql-connector-j-26.7.0.jar" \
  main.Main "$@"
