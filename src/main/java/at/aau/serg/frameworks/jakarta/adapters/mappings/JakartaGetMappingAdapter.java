package at.aau.serg.frameworks.jakarta.adapters.mappings;

import at.aau.serg.frameworks.RequestAnnotation;
import at.aau.serg.parsers.HttpMethod;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

public class JakartaGetMappingAdapter implements RequestAnnotation {
    private final GET annotation;
    private final CtMethod<?> method;

    public JakartaGetMappingAdapter(GET getAnnotation, CtMethod<?> method) {
        this.annotation = getAnnotation;
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

    @Override
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

    @Override
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
    public HttpMethod[] method() {
        return new HttpMethod[]{HttpMethod.GET};
    }

    private String[] convertToStringArray(Object value) {
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
