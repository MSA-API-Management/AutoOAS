FROM eclipse-temurin:17-jdk-alpine

ARG JAR_FILE="./target/auto-oas-1.2.0-jar-with-dependencies.jar"

WORKDIR /app/
COPY ${JAR_FILE} /app/auto-oas.jar

ENTRYPOINT [ "java", "-jar", "/app/auto-oas.jar" ]