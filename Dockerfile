# ==============================================================================
# Multi-stage Dockerfile for Bus Management & Reservation System (SwiftBus)
# Low-resource footprint: Alpine Linux + Eclipse Temurin 21 JRE
# Target hardware: Intel Pentium (1-2 Cores), 4 GB System RAM, 500 GB HDD
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build stage & Automated Verification
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

COPY src ./src
COPY lib ./lib

# Compile all Java sources (application + automated test suite)
RUN mkdir -p bin && \
    find src -name "*.java" > sources.txt && \
    javac -cp "lib/mysql-connector-j-26.7.0/mysql-connector-j-26.7.0.jar" -d bin @sources.txt && \
    rm -f sources.txt

# Run the 60-test automated verification suite during image build
RUN java -cp "bin:lib/mysql-connector-j-26.7.0/mysql-connector-j-26.7.0.jar" test.TestSuite

# ------------------------------------------------------------------------------
# Stage 2: Ultra-slim Runtime environment (Temurin Alpine JRE)
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="SwiftBus Engineering" \
      description="Pure Java Bus Reservation System with native MySQL JDBC" \
      version="2.0.0"

ENV PORT=6969 \
    DB_HOST=db \
    DB_PORT=3307 \
    DB_NAME=bus_reservation \
    DB_USER=root \
    DB_PASSWORD=root@6069 \
    PBKDF2_ITERATIONS=65000

# Install netcat for startup database dependency checking & healthchecks
RUN apk add --no-cache netcat-openbsd

WORKDIR /app

# Copy compiled classes, official MySQL connector, and entrypoint script
COPY --from=builder /build/bin /app/bin
COPY lib/mysql-connector-j-26.7.0/mysql-connector-j-26.7.0.jar /app/lib/mysql-connector-j-26.7.0.jar
COPY docker-entrypoint.sh /app/docker-entrypoint.sh

# Normalize CRLF line endings to LF and make entrypoint executable
RUN sed -i 's/\r$//' /app/docker-entrypoint.sh && chmod +x /app/docker-entrypoint.sh

EXPOSE 6969

HEALTHCHECK --interval=15s --timeout=5s --retries=3 --start-period=10s \
  CMD nc -z 127.0.0.1 6969 || exit 1

ENTRYPOINT ["/app/docker-entrypoint.sh"]
