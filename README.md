# AutoOAS: OpenAPI Generation for Java Spring Boot projects

## Compile and run

AutoOAS uses Java 21 and [Spoon](https://spoon.gforge.inria.fr/) 11, which allows analyzing projects written also in Java up to version 21.

Currently, AutoOAS supports Maven projects and Gradle projects that support POM generation via the legacy maven plugin (typically Groovy-based builds). 
Gradle projects using the Kotlin DSL (build.gradle.kts) are not supported at this time.
If the Gradle project under analysis requires an older Java version, please set the `JAVA11_HOME` environment variable to point to a compatible JDK (e.g., Java 11). 
This ensures compatibility with legacy Gradle builds during POM generation. 

Compile and run:
```shell
mvn clean package

java -jar target/auto-oas-1.2.0-jar-with-dependencies.jar <path-to-mvn-project> <oas-output-name>
```

## Evaluation
The scripts for the evaluation are located in [scripts](./scripts).

### Runtime evaluation
Execute the runtime evaluation script with the following commands. The script also generates the OpenAPI descriptions in the *<output_dir>*.

```shell
dataset_dir="/home/alex/Respector-fork/dataset"
output_dir="outputs-java21"
for _ in {1..5}; do ./scripts/run_runtime_eval.sh $dataset_dir $output_dir -jersey -spring; done

python3 scripts/calc_runtime_avg.py $output_dir/logs
# or calculate manually
```


## Docker image
We provide a Docker image for easier integration into GitHub and GitLab workflows.
```shell
docker build -t alexx882/auto-oas:1.0 .
# or
docker buildx build --push --platform=linux/amd64,linux/arm64 -t alexx882/auto-oas:1.0 .
```

```shell
docker run -v <path-to-mvn-project>:/project alexx882/auto-oas:1.0 /project /project/<oas-output-path-prefix>

# e.g., analyze current directory and write to target/docker-output/
docker run -v `pwd`:/project alexx882/auto-oas:1.0 /project /project/target/docker-output/oas
```
