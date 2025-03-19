package at.aau.serg.frameworks.spring.adapter.mapping;

import at.aau.serg.parsers.HttpMethod;
import at.aau.serg.frameworks.RequestAnnotation;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;

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

    @Override
    public HttpMethod[] method() {
        return Arrays.stream(annotation.method())
                .map(requestMethod -> HttpMethod.valueOf(requestMethod.name()))
                .toArray(HttpMethod[]::new);
    }
}
