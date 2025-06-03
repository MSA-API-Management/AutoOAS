package at.aau.serg.frameworks.jakarta.adapters.mappings;

import at.aau.serg.frameworks.RestOperationAnnotation;
import at.aau.serg.parsers.HttpMethod;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import static at.aau.serg.util.Utils.convertToStringArray;

public class JakartaRestOperationAnnotationAdapter implements RestOperationAnnotation {
    private final HttpMethod[] httpMethods;
    private final Produces produces;
    private final Consumes consumes;
    private final Path path;

    // Class-level annotations
    public JakartaRestOperationAnnotationAdapter(CtType<?> type) {
        produces = type.getAnnotation(Produces.class);
        consumes = type.getAnnotation(Consumes.class);
        path = type.getAnnotation(Path.class);
        this.httpMethods = new HttpMethod[0];
    }

    // Method-level annotations
    public JakartaRestOperationAnnotationAdapter(CtMethod<?> method, HttpMethod httpMethod) {
        produces = method.getAnnotation(Produces.class);
        consumes = method.getAnnotation(Consumes.class);
        path = method.getAnnotation(Path.class);
        this.httpMethods = new HttpMethod[] {httpMethod};
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

    @Override
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
        return httpMethods;
    }
}
