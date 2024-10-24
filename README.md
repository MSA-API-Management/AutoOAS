# OAS Generator for Java Spring Boot projects

## Compile and run

Use Java 11 for Respector's projects and Java 21 for newer projects (e.g., using Java 17 or 21). When using Java 21, also update the major Spoon version from 10 to 11.

Command for server:
```sudo update-alternatives --config java```

Command for mac:
```export JAVA_HOME=`/usr/libexec/java_home -v 11```

Compile and run:
```shell
mvn clean package

java -jar target/spring-openapi-generator-1.0.0-jar-with-dependencies.jar <path-to-mvn-project> <oas-output-name>

# example 
java -jar target/spring-openapi-generator-1.0.0-jar-with-dependencies.jar /Users/alelercher/IdeaProjects/Respector/dataset/cwa-verification-server ./target/oas-gen/cwa
```

## Eval using the prepared script
```shell
# on server
./scripts/run_all.sh /home/lerale/Respector-fork/dataset ./generated

# on mac
./scripts/run_all.sh /Users/alelercher/IdeaProjects/Respector/dataset ./generated
```

## Runtime eval
```shell
for i in {1..5}; do
  ./scripts/run_runtime_eval.sh /home/lerale/Respector-fork/dataset ./runtime_logs
done

python3 scripts/calc_runtime_avg.py ./runtime_logs/logs
# or calculate manually
```


## Docker image
We provide a Docker image for the replication package
```shell
docker build -t alexx882/oas-gen:1.0 .
# or
docker buildx build --push --platform=linux/amd64,linux/arm64 -t alexx882/oas-gen:1.0 .

docker run -v <path-to-mvn-project>:/project alexx882/oas-gen:1.0 /project /project/<oas-output-path-prefix>
docker run -v `pwd`:/project alexx882/oas-gen:1.0 /project /project/target/docker-output/oas
```

Known problems:
- _Exception in thread "main" spoon.compiler.InvalidClassPathException_:
  delete the spoon.*.tmp files which contain absolute mvn paths
