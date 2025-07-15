package at.aau.serg.frameworks.jakarta.adapters.parameters;

import at.aau.serg.frameworks.RequestHeaderAnnotation;
import javax.ws.rs.HeaderParam;
import lombok.Setter;

public class JavaxHeaderParamAdapter implements RequestHeaderAnnotation {
    private final HeaderParam annotation;

    @Setter
    boolean required = false; // optional by default

    public JavaxHeaderParamAdapter(HeaderParam annotation) {
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
