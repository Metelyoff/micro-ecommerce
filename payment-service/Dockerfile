FROM amazoncorretto:17.0.14-alpine
WORKDIR /app
COPY app.jar /app/app.jar
CMD ["sh", "-c", "java -Dserver.port=8080 -jar /app/app.jar"]

#docker buildx create --name multiarch-builder --use
#docker login
#docker buildx build --platform linux/amd64,linux/arm64 \
#  -t docker.io/metelyoff/payment-service:latest \
#  --push .