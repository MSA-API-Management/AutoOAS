package at.aau.serg.frameworks.jakarta.adapters.parameters;

import at.aau.serg.frameworks.PathVariableAnnotation;
import jakarta.ws.rs.PathParam;

public class JakartaPathParamAdapter implements PathVariableAnnotation {
    private final PathParam annotation;

    public JakartaPathParamAdapter(PathParam annotation) {
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
        return true;
    }
}
