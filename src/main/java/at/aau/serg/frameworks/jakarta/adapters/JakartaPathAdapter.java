package at.aau.serg.frameworks.jakarta.adapters;

import at.aau.serg.frameworks.RequestAnnotation;
import at.aau.serg.parsers.HttpMethod;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtElement;

import java.util.List;

public class JakartaPathAdapter implements RequestAnnotation {
    private final CtAnnotation<?> annotation;
    private final Produces produces;
    private final Consumes consumes;

    public JakartaPathAdapter(CtAnnotation<?> pathAnnotation, CtElement element) {
        this.annotation = pathAnnotation;
        this.produces = element.getAnnotation(Produces.class);
        this.consumes = element.getAnnotation(Consumes.class);
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public String[] produces() {
        if (produces != null) {
            Object value = produces.value();
            if (value != null) {
                return convertToStringArray(value);
            }
        }
        return new String[0];
    }

    @Override
    public String[] consumes() {
        if (consumes != null) {
            Object value = consumes.value();
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
        Object value = annotation.getValue("value"); // returns path in "" -> e.g. "/othersimples"
        if (value != null) {
            String pathValue = value.toString();
            if (pathValue.startsWith("\"") && pathValue.endsWith("\"")) {
                pathValue = pathValue.substring(1, pathValue.length() - 1);
            }
            return new String[]{pathValue};
        }
        return new String[0];
    }

    @Override
    public HttpMethod[] method() {
        return new HttpMethod[0];
    }

    // TODO utility
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
