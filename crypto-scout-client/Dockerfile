# MIT License
#
# Copyright (c) 2025 Andrey Karazhev
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /build

# Copy the Maven POM files
COPY pom.xml /build/
COPY crypto-scout-lib/pom.xml /build/crypto-scout-lib/
COPY crypto-scout-client/pom.xml /build/crypto-scout-client/

# Download dependencies (this layer will be cached if pom files don't change)
RUN mvn -B dependency:go-offline -f /build/pom.xml

# Copy source code
COPY crypto-scout-lib/src /build/crypto-scout-lib/src/
COPY crypto-scout-client/src /build/crypto-scout-client/src/

# Build the application
RUN mvn -B clean package -DskipTests -f /build/pom.xml

# Runtime stage
FROM eclipse-temurin:21.0.8_9-jre-ubi9-minimal

# Add metadata labels
LABEL maintainer="Andrey Karazhev"
LABEL version="0.0.1"
LABEL description="Crypto Scout Client - A Spring Boot application for crypto data processing"

# Create a non-root user to run the application
RUN microdnf install -y shadow-utils && \
    groupadd -r appuser && \
    useradd -r -g appuser -s /sbin/nologin appuser && \
    microdnf remove -y shadow-utils && \
    microdnf clean all

# Create app directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /build/crypto-scout-client/target/crypto-scout-client-0.0.1.jar /app/crypto-scout-client.jar

# Create a directory for logs with appropriate permissions
RUN mkdir -p /app/logs && \
    chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Define environment variables with defaults
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV SPRING_RABBITMQ_HOST=localhost
ENV SPRING_RABBITMQ_PORT=5672
ENV RABBITMQ_USERNAME=guest
ENV RABBITMQ_PASSWORD=guest
ENV CMC_API_KEY=""
ENV BYBIT_API_KEY=""

# Expose the application port
EXPOSE 8080

# Set health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/crypto-scout-client.jar"]