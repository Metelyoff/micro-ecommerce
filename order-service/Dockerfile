FROM amazoncorretto:17.0.14-alpine
WORKDIR /app
COPY app.jar .
ENV SERVER_PORT=8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=10s --retries=3 \
    CMD wget --quiet --spider http://localhost:${SERVER_PORT}/actuator/health || exit 1
CMD java -Dserver.port=$SERVER_PORT -jar app.jar

#docker buildx create --name multiarch-builder --use
#docker login
#docker buildx build --platform linux/amd64,linux/arm64 \
#  -t docker.io/metelyoff/order-service:latest \
#  --push .