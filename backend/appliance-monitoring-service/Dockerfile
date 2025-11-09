# Multi-stage build for Spring Boot application
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Copy the Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x ./gradlew

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build the application
RUN ./gradlew build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8092

# Set JVM options
ENV JAVA_OPTS="-Xmx512m -Xms256m -Deureka.client.serviceUrl.defaultZone=http://service-discovery:8761/eureka/"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
