package at.aau.serg.frameworks.spring.adapters.mappings;

import at.aau.serg.parsers.HttpMethod;
import at.aau.serg.frameworks.RequestAnnotation;
import org.springframework.web.bind.annotation.PutMapping;

public class SpringPutMappingAdapter implements RequestAnnotation {
    private final PutMapping annotation;

    public SpringPutMappingAdapter(PutMapping annotation) {
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
        return new HttpMethod[] { HttpMethod.PUT };
    }


}
