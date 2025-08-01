FROM gradle:jdk17 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew clean build --no-daemon

FROM amazoncorretto:17.0.14-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar /app/app.jar
CMD java -Dspring.profiles.active=${ENV_PROFILE} -Dserver.port=${SERVER_PORT} -jar /app/app.jar

# Build and push commands
# docker buildx create --name multiarch-builder --use
# docker login
# docker buildx build --platform linux/amd64,linux/arm64 \
#   -t docker.io/metelyoff/inventory-service:latest \
#   --push .
