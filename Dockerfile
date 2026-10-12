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
# Stage 2: Runtime environment (Pure Java Web Server on Port 6969)
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre

ENV DEBIAN_FRONTEND=noninteractive \
    PORT=6969 \
    DB_HOST=db \
    DB_PORT=3307 \
    DB_NAME=bus_reservation \
    DB_USER=root \
    DB_PASSWORD=root@6069

# Install netcat for startup database dependency checking
RUN apt-get update && apt-get install -y --no-install-recommends \
    netcat-openbsd \
    procps \
    && rm -rf /var/lib/apt/lists/*

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
