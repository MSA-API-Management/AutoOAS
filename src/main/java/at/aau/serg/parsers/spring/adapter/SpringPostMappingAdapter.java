package at.aau.serg.parsers.spring.adapter;

import at.aau.serg.parsers.HttpMethod;
import at.aau.serg.interfaces.RequestAnnotation;
import org.springframework.web.bind.annotation.PostMapping;

public class SpringPostMappingAdapter implements RequestAnnotation {
    private final PostMapping annotation;

    public SpringPostMappingAdapter(PostMapping annotation) {
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
        return new HttpMethod[] { HttpMethod.POST };
    }


}
