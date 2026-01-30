FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add a non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copy the jar file
COPY target/*.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app

USER appuser

# Expose the port
EXPOSE 8087

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8087/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
