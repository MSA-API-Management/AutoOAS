# AutoOAS: OpenAPI Generation for Java Spring Boot projects

## Compile and run

AutoOAS uses Java 21 and [Spoon](https://spoon.gforge.inria.fr/) 11, which allows analyzing projects written also in Java up to version 21.

Compile and run:
```shell
mvn clean package

java -jar target/auto-oas-1.1.0-jar-with-dependencies.jar <path-to-mvn-project> <oas-output-name>
```

## Evaluation
The scripts for the evaluation are located in [scripts](./scripts).

### Runtime evaluation
Execute the runtime evaluation script with the following commands. The script also generates the OpenAPI descriptions in the *<output_dir>*.

```shell
dataset_dir="/home/alex/Respector-fork/dataset"
output_dir="outputs-java21-jersey"
for _ in {1..1}; do ./scripts/run_runtime_eval.sh $dataset_dir $output_dir; done

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
