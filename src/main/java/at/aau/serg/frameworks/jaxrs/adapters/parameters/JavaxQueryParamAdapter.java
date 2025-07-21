package at.aau.serg.frameworks.jaxrs.adapters.parameters;

import at.aau.serg.frameworks.RequestParamAnnotation;
import javax.ws.rs.QueryParam;
import lombok.Setter;

public class JavaxQueryParamAdapter implements RequestParamAnnotation {
    private final QueryParam annotation;

    @Setter
    private boolean required = false; // optional by default

    public JavaxQueryParamAdapter(QueryParam annotation) {
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
