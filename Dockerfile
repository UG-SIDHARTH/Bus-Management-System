# ==============================================================================
# Multi-stage Dockerfile for Bus Management & Reservation System (Java Swing GUI)
# Exposes an interactive web-based GUI via noVNC & websockify on Port 6969
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build stage
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /build

COPY src ./src
COPY lib ./lib

# Compile all Java sources using the bundled MySQL JDBC driver
RUN mkdir -p bin && \
    find src -name "*.java" > sources.txt && \
    javac -cp "lib/mysql-connector-j-26.7.0/mysql-connector-j-26.7.0.jar" -d bin @sources.txt

# ------------------------------------------------------------------------------
# Stage 2: Runtime environment with virtual display and noVNC web interface
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre

ENV DEBIAN_FRONTEND=noninteractive \
    DISPLAY=:0 \
    PORT=6969 \
    RESOLUTION=1280x800x24 \
    DB_HOST=db \
    DB_PORT=3307 \
    DB_NAME=bus_reservation \
    DB_USER=root \
    DB_PASSWORD=root@6069

# Install virtual framebuffer (Xvfb), lightweight window manager (fluxbox),
# VNC server (x11vnc), noVNC HTML5 client, websockify, and TrueType fonts
RUN apt-get update && apt-get install -y --no-install-recommends \
    xvfb \
    x11vnc \
    novnc \
    websockify \
    fluxbox \
    netcat-openbsd \
    fonts-dejavu-core \
    procps \
    && rm -rf /var/lib/apt/lists/*

# Configure default noVNC page to automatically connect and resize to browser window
RUN if [ -d /usr/share/novnc ]; then \
      echo '<!DOCTYPE html><html><head><meta http-equiv="refresh" content="0; url='\''vnc.html?autoconnect=true&resize=remote&reconnect=true'\''" /><title>Bus Management System</title></head><body>Redirecting to Bus Management System... <a href="vnc.html?autoconnect=true&resize=remote&reconnect=true">Click here if not redirected</a></body></html>' > /usr/share/novnc/index.html; \
    fi

WORKDIR /app

# Copy compiled classes, MySQL driver, and entrypoint script
COPY --from=builder /build/bin /app/bin
COPY lib/mysql-connector-j-26.7.0/mysql-connector-j-26.7.0.jar /app/lib/mysql-connector-j-26.7.0.jar
COPY docker-entrypoint.sh /app/docker-entrypoint.sh

# Normalize CRLF line endings to LF and make entrypoint executable
RUN sed -i 's/\r$//' /app/docker-entrypoint.sh && chmod +x /app/docker-entrypoint.sh

# Web UI port (noVNC)
EXPOSE 6969

ENTRYPOINT ["/app/docker-entrypoint.sh"]
