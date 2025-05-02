package at.aau.serg.frameworks.jakarta.adapters.mappings;

import at.aau.serg.parsers.HttpMethod;
import jakarta.ws.rs.DELETE;
import spoon.reflect.declaration.CtMethod;

public class JakartaDeleteMappingAdapter extends AbstractJakartaHttpMethodAdapter {
    public JakartaDeleteMappingAdapter(DELETE deleteAnnotation, CtMethod<?> method) {
        super(deleteAnnotation, method);
    }

    @Override
    public HttpMethod[] method() {
        return new HttpMethod[]{HttpMethod.DELETE};
    }
}
