package at.aau.serg.frameworks.jakarta.adapters.mappings;

import at.aau.serg.parsers.HttpMethod;
import jakarta.ws.rs.PUT;
import spoon.reflect.declaration.CtMethod;

public class JakartaPutMappingAdapter extends AbstractJakartaHttpMethodAdapter {
    public JakartaPutMappingAdapter(PUT putAnnotation, CtMethod<?> method) {
        super(putAnnotation, method);
    }

    @Override
    public HttpMethod[] method() {
        return new HttpMethod[]{HttpMethod.PUT};
    }
}
