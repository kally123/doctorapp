# Multi-stage build for optimized Docker image

# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/

# Download dependencies (fix line endings for Alpine)
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw dependency:go-offline -B -pl api-gateway

# Copy source code
COPY api-gateway/src api-gateway/src

# Build the application
RUN ./mvnw clean package -pl api-gateway -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add non-root user
RUN addgroup -g 1000 appgroup && adduser -u 1000 -G appgroup -D appuser

# Copy the built JAR
COPY --from=builder /app/api-gateway/target/*.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app

USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
