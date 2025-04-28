package at.aau.serg.frameworks.jakarta.adapters.parameters;

import at.aau.serg.frameworks.RequestHeaderAnnotation;
import jakarta.ws.rs.HeaderParam;

public class JakartaHeaderParamAdapter implements RequestHeaderAnnotation {
    private final HeaderParam annotation;

    public JakartaHeaderParamAdapter(HeaderParam annotation) {
        this.annotation = annotation;
    }

    @Override
    public String value() {
        return annotation.value();
    }

    @Override
    public String name() {
        return annotation.value();
    }

    @Override
    public boolean required() {
        return false; // optional by default
    }
}
