package at.aau.serg.frameworks.jakarta.adapters.mappings;

import at.aau.serg.parsers.HttpMethod;
import jakarta.ws.rs.POST;
import spoon.reflect.declaration.CtMethod;

public class JakartaPostMappingAdapter extends AbstractJakartaHttpMethodAdapter {
    public JakartaPostMappingAdapter(POST postAnnotation, CtMethod<?> method) {
        super(postAnnotation, method);
    }

    @Override
    public HttpMethod[] method() {
        return new HttpMethod[]{HttpMethod.POST};
    }
}
