# AutoOAS: Generating accurate OpenAPI descriptions from Java source code

[AutoOAS](https://doi.org/10.1016/j.jss.2026.113122) supports the Java frameworks Spring Boot and JAX-RS (`javax.ws.rs.*` and `jakarta.ws.rs.*`).

## Compile and run

AutoOAS uses Java 21 and [Spoon](https://spoon.gforge.inria.fr/) 11, which allows analyzing projects written in Java up to version 21.

It supports Maven projects and Gradle projects that support POM generation via the legacy maven plugin (typically Groovy-based builds). 
Gradle projects using the Kotlin DSL (build.gradle.kts) are not supported at this time.
If the Gradle project under analysis requires an older Java version, please set the `JAVA11_HOME` environment variable to point to a compatible JDK (e.g., Java 11). 
This ensures compatibility with legacy Gradle builds during POM generation. 

Compile and run:
```shell
mvn clean package

java -jar target/auto-oas-1.2.0-jar-with-dependencies.jar <path-to-mvn-project> <oas-output-name>
```

## Evaluation
The script for the evaluation is located in [scripts](./scripts).

### Runtime evaluation
Execute the runtime evaluation script with the following commands. The script also generates the OpenAPI descriptions in the *<output_dir>*.

```shell
mvn clean package -Dmaven.test.skip=true

dataset_dir="path/to/dataset"
output_dir="outputs"
rm -r $output_dir
for _ in {1..5}; do ./scripts/run_runtime_eval.sh $dataset_dir $output_dir -jersey -spring ; done
```

## Docker image
We provide a [Docker image](https://hub.docker.com/r/alexx882/auto-oas) for easier integration into GitHub and GitLab workflows.
```shell
docker build -t alexx882/auto-oas:1.2 .
# or
docker buildx build --push --platform=linux/amd64,linux/arm64 -t alexx882/auto-oas:1.2 .
```

```shell
docker run -v <path-to-mvn-project>:/project alexx882/auto-oas:1.2 /project /project/<oas-output-path-prefix>

# e.g., analyze current directory and write to target/docker-output/
docker run -v `pwd`:/project alexx882/auto-oas:1.2 /project /project/target/docker-output/oas
```

## Academic Use
If you use this project in your academic work, please cite the following paper:

> A. Lercher, D. Jamnig, C. Macho, C. Bauer, and M. Pinzger, “Generating accurate OpenAPI descriptions from Java source code,” Journal of Systems and Software, vol. 244, p. 113122, 2027.

```bibtex
@article{LERCHER2027113122,
  title = {Generating accurate OpenAPI descriptions from Java source code},
  journal = {Journal of Systems and Software},
  volume = {244},
  pages = {113122},
  year = {2027},
  issn = {0164-1212},
  doi = {https://doi.org/10.1016/j.jss.2026.113122},
  url = {https://www.sciencedirect.com/science/article/pii/S0164121226003559},
  author = {Alexander Lercher and David Jamnig and Christian Macho and Clemens Bauer and Martin Pinzger}
  }
```
