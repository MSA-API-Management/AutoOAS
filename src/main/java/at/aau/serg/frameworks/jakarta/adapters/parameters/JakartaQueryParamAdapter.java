package at.aau.serg.frameworks.jakarta.adapters.parameters;

import at.aau.serg.frameworks.RequestParamAnnotation;
import jakarta.ws.rs.QueryParam;

public class JakartaQueryParamAdapter implements RequestParamAnnotation {
    private final QueryParam annotation;

    public JakartaQueryParamAdapter(QueryParam annotation) {
        this.annotation = annotation;
    }

    @Override
    public String value() {
        return annotation.value();
    }

    @Override
    public boolean required() {
        return false; // optional by default
    }
}
