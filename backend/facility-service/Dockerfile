FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8089

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]
