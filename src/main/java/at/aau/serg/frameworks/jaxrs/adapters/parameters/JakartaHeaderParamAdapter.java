package at.aau.serg.frameworks.jaxrs.adapters.parameters;

import at.aau.serg.frameworks.RequestHeaderAnnotation;
import jakarta.ws.rs.HeaderParam;
import lombok.Setter;

public class JakartaHeaderParamAdapter implements RequestHeaderAnnotation {
    private final HeaderParam annotation;

    @Setter
    boolean required = false; // optional by default

    public JakartaHeaderParamAdapter(HeaderParam annotation) {
        this.annotation = annotation;
    }

    @Override
    public String value() {
        return annotation.value();
    }

    @Override
    public boolean required() {
        return required;
    }
}
