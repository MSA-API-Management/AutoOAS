package at.aau.serg.frameworks.spring.adapters.parameters;

import at.aau.serg.frameworks.PathVariableAnnotation;
import org.springframework.web.bind.annotation.PathVariable;

public class SpringPathVariableAdapter implements PathVariableAnnotation {
    private final PathVariable annotation;

    public SpringPathVariableAdapter(PathVariable annotation) {
        this.annotation = annotation;
    }

    @Override
    public String value() {
        return annotation.value();
    }

    @Override
    public boolean required() {
        return annotation.required();
    }
}
