package at.aau.serg.frameworks.spring.adapters.mappings;

import at.aau.serg.parsers.HttpMethod;
import at.aau.serg.frameworks.RestOperationAnnotation;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;

public class SpringRequestMappingAdapter implements RestOperationAnnotation {
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
    public String[] path() {
        return annotation.path().length > 0 ? annotation.path() : annotation.value();
    }

    @Override
    public HttpMethod[] method() {
        return Arrays.stream(annotation.method())
                .map(requestMethod -> HttpMethod.valueOf(requestMethod.name()))
                .toArray(HttpMethod[]::new);
    }
}
