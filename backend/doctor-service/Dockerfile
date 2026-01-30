# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY healthcare-common/pom.xml healthcare-common/
COPY doctor-service/pom.xml doctor-service/

# Download dependencies (fix line endings for Alpine)
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && \
    ./mvnw dependency:go-offline -pl doctor-service -am -B

# Copy source code
COPY healthcare-common/src healthcare-common/src
COPY doctor-service/src doctor-service/src

# Build application
RUN ./mvnw clean package -pl doctor-service -am -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copy JAR from builder
COPY --from=builder /app/doctor-service/target/*.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app

USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/health || exit 1

# Expose port
EXPOSE 8082

# JVM options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
