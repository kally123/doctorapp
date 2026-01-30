# Multi-stage build for optimized Docker image

# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY healthcare-common/pom.xml healthcare-common/
COPY user-service/pom.xml user-service/

# Download dependencies (fix line endings for Alpine)
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code
COPY healthcare-common/src healthcare-common/src
COPY user-service/src user-service/src

# Build the application
RUN ./mvnw clean package -pl user-service -am -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add non-root user
RUN addgroup -g 1000 appgroup && adduser -u 1000 -G appgroup -D appuser

# Copy the built JAR
COPY --from=builder /app/user-service/target/*.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app

USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Expose port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
