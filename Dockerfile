FROM  ubuntu:24.10
# --platform=linux/amd64

RUN apt-get update
RUN apt-get install openjdk-17-jdk -y
RUN apt-get install maven -y

ARG JAR_FILE="./target/auto-oas-1.0.0-jar-with-dependencies.jar"

WORKDIR /app/
COPY ${JAR_FILE} /app/auto-oas.jar

ENTRYPOINT [ "java", "-jar", "/app/auto-oas.jar" ]