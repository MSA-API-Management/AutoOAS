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
    private final CtElement element;

    public JakartaPathAdapter(CtAnnotation<?> pathAnnotation, CtElement element) {
        this.annotation = pathAnnotation;
        this.element = element;
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public String[] produces() {
        for (CtAnnotation<?> anno : element.getAnnotations()) {
            String annotationTypeName = anno.getAnnotationType().getQualifiedName();
            if (annotationTypeName.equals(Produces.class.getName())) {
                Object value = anno.getValue("value");
                if (value != null) {
                    return convertToStringArray(value);
                }
            }
        }
        return new String[0];
    }

    @Override
    public String[] consumes() {
        for (CtAnnotation<?> anno : element.getAnnotations()) {
            String annotationTypeName = anno.getAnnotationType().getQualifiedName();
            if (annotationTypeName.equals(Consumes.class.getName())) {
                Object value = anno.getValue("value");
                if (value != null) {
                    return convertToStringArray(value);
                }
            }
        }
        return new String[0];
    }

    @Override
    public String[] value() {
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
    public String[] path() {
        return value();
    }

    @Override
    public HttpMethod[] method() {
        return new HttpMethod[0];
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
