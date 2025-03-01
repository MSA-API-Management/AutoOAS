package at.aau.serg.parsers.spring;

import at.aau.serg.parsers.RequestAnnotation;
import org.springframework.web.bind.annotation.PatchMapping;

public class SpringPatchMappingAdapter implements RequestAnnotation {
    private final PatchMapping annotation;

    public SpringPatchMappingAdapter(PatchMapping annotation) {
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
