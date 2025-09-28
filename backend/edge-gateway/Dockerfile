# Multi-stage build for SmartWatts Edge Gateway

# Stage 1: Build the application
FROM openjdk:17-jdk-slim AS builder

WORKDIR /app

# Copy gradle files
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src/ src/

# Make gradlew executable and build the application
RUN chmod +x gradlew && \
    ./gradlew clean build -x test

# Stage 2: Run the application
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built JAR file from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the port
EXPOSE 8088

# Set JVM options for edge computing
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:+UseStringDeduplication"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
