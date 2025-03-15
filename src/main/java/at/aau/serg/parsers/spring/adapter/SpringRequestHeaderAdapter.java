package at.aau.serg.parsers.spring.adapter;

import at.aau.serg.interfaces.RequestHeaderAnnotation;
import org.springframework.web.bind.annotation.RequestHeader;

public class SpringRequestHeaderAdapter implements RequestHeaderAnnotation {
    private final RequestHeader annotation;

    public SpringRequestHeaderAdapter(RequestHeader annotation) {
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
