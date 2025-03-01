package at.aau.serg.parsers.spring;

import at.aau.serg.parsers.RequestAnnotation;
import org.springframework.web.bind.annotation.RequestMapping;

public class SpringRequestMappingAdapter implements RequestAnnotation {
    private final RequestMapping annotation;

    public SpringRequestMappingAdapter(RequestMapping annotation) {
        this.annotation = annotation;
    }

    @Override
    public String name() {
        return annotation.name();
    }

    @Override
    public String[] produces() {
        return annotation.produces();
    }

    @Override
    public String[] consumes() {
        return annotation.consumes();
    }

    @Override
    public String[] value() {
        return annotation.value();
    }

    @Override
    public String[] path() {
        return annotation.path();
    }
}
