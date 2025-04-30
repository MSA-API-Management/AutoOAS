package at.aau.serg.frameworks.jakarta.adapters.mappings;

import at.aau.serg.frameworks.RequestAnnotation;
import at.aau.serg.parsers.HttpMethod;
import jakarta.ws.rs.DELETE;

public class JakartaDeleteMappingAdapter implements RequestAnnotation {
    private final DELETE annotation;

    public JakartaDeleteMappingAdapter(DELETE annotation) {
        this.annotation = annotation;
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
