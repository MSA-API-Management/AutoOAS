FROM eclipse-temurin:21-jdk-alpine

RUN apk add --no-cache maven
ARG JAR_FILE="./target/auto-oas-1.2.0-jar-with-dependencies.jar"

WORKDIR /app/
COPY ${JAR_FILE} /app/auto-oas.jar

ENTRYPOINT [ "java", "-jar", "/app/auto-oas.jar" ]