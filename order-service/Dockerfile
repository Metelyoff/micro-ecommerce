FROM gradle:jdk17 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew clean build --no-daemon

FROM openjdk:17-jdk-slim
WORKDIR /app
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY --from=builder /app/build/libs/*.jar /app/app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 CMD curl -f http://localhost:8080/ || exit 1
ENTRYPOINT ["java", "-Dserver.port=8080", "-jar", "/app/app.jar"]
