# Multi-stage build for Spring Boot application
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Copy gradle files
COPY gradlew .
RUN mkdir -p gradle/wrapper
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.jar
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.properties
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build the application
RUN ./gradlew build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8081

# Set JVM options
ENV JAVA_OPTS="-Xmx512m -Xms256m -Deureka.client.serviceUrl.defaultZone=http://service-discovery:8761/eureka/"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 