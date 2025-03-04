package at.aau.serg.parsers.spring;

import at.aau.serg.parsers.PathVariableAnnotation;
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
    public String name() {
        return annotation.name();
    }

    @Override
    public boolean required() {
        return annotation.required();
    }
}
