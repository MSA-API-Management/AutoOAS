package at.aau.serg.frameworks.jakarta.adapters.mappings;

import at.aau.serg.parsers.HttpMethod;
import jakarta.ws.rs.GET;
import spoon.reflect.declaration.CtMethod;

public class JakartaGetMappingAdapter extends AbstractJakartaHttpMethodAdapter {
    public JakartaGetMappingAdapter(GET getAnnotation, CtMethod<?> method) {
        super(getAnnotation, method);
    }

    @Override
    public HttpMethod[] method() {
        return new HttpMethod[]{HttpMethod.GET};
    }
}
