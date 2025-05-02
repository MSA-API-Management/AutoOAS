package at.aau.serg.frameworks.jakarta.adapters.mappings;

import at.aau.serg.frameworks.RequestAnnotation;
import at.aau.serg.parsers.HttpMethod;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import spoon.reflect.declaration.CtMethod;

import java.lang.annotation.Annotation;
import java.util.List;

public abstract class AbstractJakartaHttpMethodAdapter implements RequestAnnotation {
    protected final Annotation methodAnnotation;
    protected final CtMethod<?> method;

    protected AbstractJakartaHttpMethodAdapter(Annotation methodAnnotation, CtMethod<?> method) {
        this.methodAnnotation = methodAnnotation;
        this.method = method;
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public String[] produces() {
        Produces annotation = method.getAnnotation(Produces.class);
        if (annotation != null) {
            Object value = annotation.value();
            if (value != null) {
                return convertToStringArray(value);
            }
        }
        return new String[0];
    }

    public String[] consumes() {
        Consumes annotation = method.getAnnotation(Consumes.class);
        if (annotation != null) {
            Object value = annotation.value();
            if (value != null) {
                return convertToStringArray(value);
            }
        }
        return new String[0];
    }

    @Override
    public String[] value() {
        return path();
    }

    public String[] path() {
        Path annotation = method.getAnnotation(Path.class);
        if (annotation != null) {
            Object value = annotation.value();
            if (value != null) {
                return new String[]{value.toString()};
            }
        }
        return new String[0];
    }

    @Override
    public abstract HttpMethod[] method();

    // TODO utility
    protected String[] convertToStringArray(Object value) {
        if (value instanceof String[]) {
            return (String[]) value;
        } else if (value instanceof List) {
            List<?> list = (List<?>) value;
            return list.stream().map(Object::toString).toArray(String[]::new);
        } else {
            return new String[]{value.toString()};
        }
    }
}
