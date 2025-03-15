package at.aau.serg.parsers.spring;

import at.aau.serg.parsers.HttpMethod;
import at.aau.serg.interfaces.RequestAnnotation;
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
