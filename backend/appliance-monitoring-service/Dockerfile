FROM openjdk:17-jdk-slim

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

# Create a new stage for runtime
FROM openjdk:17-slim

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=0 /app/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8092

# Set JVM options
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
