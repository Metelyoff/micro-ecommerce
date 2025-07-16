FROM gradle:jdk17 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew clean build --no-daemon

FROM amazoncorretto:17.0.14-alpine
WORKDIR /app
RUN apk update && apk add --no-cache curl
COPY --from=builder /app/build/libs/*.jar /app/app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 CMD curl -f http://localhost:8080/ || exit 1
ENTRYPOINT ["java", "-Dserver.port=8080", "-jar", "/app/app.jar"]
#docker buildx create --name multiarch-builder --use
#docker login
#docker buildx build --platform linux/amd64,linux/arm64 \
#  -t docker.io/metelyoff/order-service:latest \
#  --push .