FROM openjdk:25-slim

WORKDIR /app

# Install Python and dependencies
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    chromium-browser \
    firefox-esr \
    && rm -rf /var/lib/apt/lists/*

# Copy backend JAR
COPY build/libs/auto-operation-0.0.1-SNAPSHOT.jar app.jar

# Copy Python scripts and templates
COPY scripts/ /app/scripts/

# Install Python dependencies
RUN pip3 install -r /app/scripts/requirements.txt

# Expose ports
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
