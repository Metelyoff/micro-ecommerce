FROM gradle:jdk17 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew clean build --no-daemon publish
