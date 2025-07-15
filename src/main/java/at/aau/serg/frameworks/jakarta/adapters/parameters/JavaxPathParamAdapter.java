package at.aau.serg.frameworks.jakarta.adapters.parameters;

import at.aau.serg.frameworks.PathVariableAnnotation;
import javax.ws.rs.PathParam;

public class JavaxPathParamAdapter implements PathVariableAnnotation {
    private final PathParam annotation;

    public JavaxPathParamAdapter(PathParam annotation) {
        this.annotation = annotation;
    }

    @Override
    public String value() {
        return annotation.value();
    }

    @Override
    public boolean required() {
        return true;
    }
}
