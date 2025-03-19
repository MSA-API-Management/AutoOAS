package at.aau.serg.frameworks.spring.adapter.annotation;

import at.aau.serg.frameworks.RequestParamAnnotation;
import org.springframework.web.bind.annotation.RequestParam;

public class SpringRequestParamAdapter implements RequestParamAnnotation {
    private final RequestParam annotation;

    public SpringRequestParamAdapter(RequestParam annotation) {
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
