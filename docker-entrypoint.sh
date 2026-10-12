#!/bin/sh
set -e

# Default environment variables
PORT=${PORT:-6969}
DB_HOST=${DB_HOST:-db}
DB_PORT=${DB_PORT:-3307}

echo "=================================================="
echo " 🚍 Starting SwiftBus Web Management System"
echo " Web Portal Port: ${PORT}"
echo " Database Target: ${DB_HOST}:${DB_PORT}"
echo " Hardware Profile: Low-spec Pentium / 512MB Cgroup"
echo "=================================================="

# Wait for MySQL to become available if DB_HOST is set
if [ -n "$DB_HOST" ] && [ "$DB_HOST" != "localhost" ]; then
    echo "Checking database availability at ${DB_HOST}:${DB_PORT}..."
    MAX_RETRIES=40
    COUNT=0
    until nc -z -w 2 "$DB_HOST" "$DB_PORT" 2>/dev/null; do
        COUNT=$((COUNT + 1))
        if [ "$COUNT" -ge "$MAX_RETRIES" ]; then
            echo "Warning: Database not ready after ${MAX_RETRIES} attempts, proceeding to start..."
            break
        fi
        echo "Waiting for database to initialize (${COUNT}/${MAX_RETRIES})..."
        sleep 1
    done
    echo "Database connection check passed!"
fi

echo "=================================================="
echo " Launching SwiftBus Native Java Web Server..."
echo " Access at: http://localhost:${PORT}"
echo " JVM Settings: -Xms128m -Xmx384m -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"
echo "=================================================="

exec java \
  -Xms128m \
  -Xmx384m \
  -XX:+UseSerialGC \
  -XX:TieredStopAtLevel=1 \
  -Xss512k \
  -cp "/app/bin:/app/lib/mysql-connector-j-26.7.0.jar" \
  main.Main
