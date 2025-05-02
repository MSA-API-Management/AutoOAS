package at.aau.serg.frameworks.jakarta.adapters.mappings;

import at.aau.serg.frameworks.RequestAnnotation;
import at.aau.serg.parsers.HttpMethod;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import spoon.reflect.declaration.CtMethod;

import static at.aau.serg.util.Utils.convertToStringArray;

public class JakartaHttpMethodAdapter implements RequestAnnotation {
    private final HttpMethod httpMethod;
    private final Produces produces;
    private final Consumes consumes;
    private final Path path;

    public JakartaHttpMethodAdapter(CtMethod<?> method, HttpMethod httpMethod) {
        produces = method.getAnnotation(Produces.class);
        consumes = method.getAnnotation(Consumes.class);
        path = method.getAnnotation(Path.class);
        this.httpMethod = httpMethod;
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

    public String[] path() {
        if (path != null) {
            Object value = path.value();
            if (value != null) {
                return new String[]{value.toString()};
            }
        }
        return new String[0];
    }

    @Override
    public HttpMethod[] method() {
        return new HttpMethod[]{httpMethod};
    }
}
