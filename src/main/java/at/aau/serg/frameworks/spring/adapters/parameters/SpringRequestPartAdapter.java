package at.aau.serg.frameworks.spring.adapters.parameters;

import at.aau.serg.frameworks.MultipartParameterAnnotation;
import org.springframework.web.bind.annotation.RequestPart;

public class SpringRequestPartAdapter implements MultipartParameterAnnotation {
    private final RequestPart annotation;

    public SpringRequestPartAdapter(RequestPart annotation) {
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
