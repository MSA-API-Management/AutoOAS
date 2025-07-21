package at.aau.serg.frameworks.jaxrs.adapters.parameters;

import at.aau.serg.frameworks.RequestParamAnnotation;
import jakarta.ws.rs.QueryParam;
import lombok.Setter;

public class JakartaQueryParamAdapter implements RequestParamAnnotation {
    private final QueryParam annotation;

    @Setter
    private boolean required = false; // optional by default

    public JakartaQueryParamAdapter(QueryParam annotation) {
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
