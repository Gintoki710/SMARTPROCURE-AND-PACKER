# ==============================================================================
# Multi-stage Dockerfile for Smart Procurement Budgeter & Warehouse Box Packer
# Zero external dependencies - Runs on minimal Alpine JRE
# ==============================================================================

# Stage 1: Build & Compile Java Sources
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN mkdir -p bin && javac -d bin $(find src -name "*.java")

# Stage 2: Lightweight Production Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/bin ./bin
COPY --from=builder /app/web ./web

# Cloud platforms automatically provide $PORT
ENV PORT=8080
EXPOSE 8080

# Launch server
CMD ["java", "-cp", "bin", "server.HttpServerApp"]
