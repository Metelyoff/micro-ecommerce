FROM gradle:jdk17 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew clean build --no-daemon

FROM amazoncorretto:17.0.14-alpine
WORKDIR /app
RUN apk update && apk add --no-cache curl
COPY --from=builder /app/build/libs/*.jar /app/app.jar
EXPOSE $SERVER_PORT
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 CMD curl -f http://localhost:$SERVER_PORT/ || exit 1
CMD ["sh", "-c", "java -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -Dserver.port=$SERVER_PORT -jar /app/app.jar"]

# Build and push commands
# docker buildx create --name multiarch-builder --use
# docker login
# docker buildx build --platform linux/amd64,linux/arm64 \
#   -t docker.io/metelyoff/inventory-service:latest \
#   --push .
