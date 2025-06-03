package at.aau.serg.frameworks.spring.adapters.parameters;

import at.aau.serg.frameworks.RequestHeaderAnnotation;
import org.springframework.web.bind.annotation.RequestHeader;

public class SpringRequestHeaderAdapter implements RequestHeaderAnnotation {
    private final RequestHeader annotation;

    public SpringRequestHeaderAdapter(RequestHeader annotation) {
        this.annotation = annotation;
    }

    @Override
    public String value() {
        return !annotation.value().isEmpty() ? annotation.value() : annotation.name();
    }

    @Override
    public boolean required() {
        return annotation.required();
    }
}
