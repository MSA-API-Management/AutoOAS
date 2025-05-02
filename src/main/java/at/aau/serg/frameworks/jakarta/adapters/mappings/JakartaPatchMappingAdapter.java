package at.aau.serg.frameworks.jakarta.adapters.mappings;

import at.aau.serg.parsers.HttpMethod;
import jakarta.ws.rs.PATCH;
import spoon.reflect.declaration.CtMethod;

public class JakartaPatchMappingAdapter extends AbstractJakartaHttpMethodAdapter {
    public JakartaPatchMappingAdapter(PATCH patchAnnotation, CtMethod<?> method) {
        super(patchAnnotation, method);
    }

    @Override
    public HttpMethod[] method() {
        return new HttpMethod[]{HttpMethod.PATCH};
    }
}
