package at.aau.serg.frameworks.jakarta.adapters.mappings;

import at.aau.serg.frameworks.RequestAnnotation;
import at.aau.serg.parsers.HttpMethod;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import spoon.reflect.declaration.CtMethod;

public class JakartaPutMappingAdapter implements RequestAnnotation {
    private final PUT annotation;
    private final CtMethod<?> method;

    public JakartaPutMappingAdapter(PUT annotation, CtMethod<?> method) {
        this.annotation = annotation;
        this.method = method;
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public String[] produces() {
        return new String[0];
    }

    @Override
    public String[] consumes() {
        return new String[0];
    }

    @Override
    public String[] value() {
        return new String[0];
    }

    @Override
    public String[] path() {
        return new String[0];
    }

    @Override
    public HttpMethod[] method() {
        return new HttpMethod[0];
    }
}
