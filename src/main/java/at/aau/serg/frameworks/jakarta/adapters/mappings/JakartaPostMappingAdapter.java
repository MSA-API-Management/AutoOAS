package at.aau.serg.frameworks.jakarta.adapters.mappings;

import at.aau.serg.frameworks.RequestAnnotation;
import at.aau.serg.parsers.HttpMethod;
import jakarta.ws.rs.POST;

public class JakartaPostMappingAdapter implements RequestAnnotation {
    private final POST annotation;

    public JakartaPostMappingAdapter(POST annotation) {
        this.annotation = annotation;
    }

    // TODO additional adapter to retrieve values

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
