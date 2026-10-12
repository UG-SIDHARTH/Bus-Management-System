#!/bin/bash
set -e

# Default environment variables
PORT=${PORT:-6969}
DISPLAY_NUM=${DISPLAY_NUM:-0}
RESOLUTION=${RESOLUTION:-1280x800x24}
DB_HOST=${DB_HOST:-db}
DB_PORT=${DB_PORT:-3306}

export DISPLAY=:${DISPLAY_NUM}

echo "=================================================="
echo " Starting Bus Management System"
echo " Web UI Port: ${PORT}"
echo " Database Host: ${DB_HOST}:${DB_PORT}"
echo "=================================================="

# Function to gracefully handle signals
cleanup() {
    echo "Shutting down services..."
    kill 0
    exit 0
}
trap cleanup SIGTERM SIGINT

# Wait for MySQL to become available if DB_HOST is set
if [ -n "$DB_HOST" ] && [ "$DB_HOST" != "localhost" ]; then
    echo "Checking connection to database at ${DB_HOST}:${DB_PORT}..."
    MAX_RETRIES=30
    COUNT=0
    until nc -z -w 2 "$DB_HOST" "$DB_PORT" 2>/dev/null; do
        COUNT=$((COUNT + 1))
        if [ "$COUNT" -ge "$MAX_RETRIES" ]; then
            echo "Warning: Database not ready after ${MAX_RETRIES} attempts, proceeding anyway..."
            break
        fi
        echo "Waiting for database (${COUNT}/${MAX_RETRIES})..."
        sleep 1
    done
    echo "Database check complete."
fi

# Clean up any leftover X11 lock files from unclean shutdowns
rm -f /tmp/.X${DISPLAY_NUM}-lock /tmp/.X11-unix/X${DISPLAY_NUM}

# 1. Start Xvfb (Virtual Framebuffer)
echo "Starting Xvfb on display :${DISPLAY_NUM} (${RESOLUTION})..."
Xvfb :${DISPLAY_NUM} -screen 0 ${RESOLUTION} &
sleep 1

# 2. Start Window Manager (Fluxbox)
echo "Starting Fluxbox window manager..."
fluxbox &
sleep 1

# 3. Start x11vnc server
echo "Starting x11vnc server on port 5900..."
x11vnc -display :${DISPLAY_NUM} -nopw -listen 127.0.0.1 -forever -shared -rfbport 5900 -quiet &
sleep 1

# 4. Start Websockify / noVNC on specified PORT
echo "Starting noVNC web server on port ${PORT}..."
websockify --web=/usr/share/novnc/ "${PORT}" 127.0.0.1:5900 &
sleep 1

echo "=================================================="
echo " Ready! Access the application in your browser at:"
echo " http://localhost:${PORT}"
echo "=================================================="

# 5. Launch Java Application in a loop so closing the window restarts it
while true; do
    echo "Launching Java Swing application (main.Main)..."
    java -cp "/app/bin:/app/lib/mysql-connector-j-26.7.0.jar" main.Main || true
    echo "Application process ended. Restarting in 2 seconds (stop container to exit)..."
    sleep 2
done
