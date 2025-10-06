package at.aau.serg.interceptors;

import com.github.jrcodeza.schema.generator.DataTypeTransformer;
import com.github.jrcodeza.schema.generator.MethodResponseExtractor;
import com.github.jrcodeza.schema.generator.util.SchemaGeneratorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.List;

public class JavaxOperationResponseCodeInterceptor extends AbstractJaxRsOperationResponseCodeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JavaxOperationResponseCodeInterceptor.class);

    public JavaxOperationResponseCodeInterceptor(List<CtType<?>> globalExceptionHandlerClasses,
                                                 DataTypeTransformer dataTypeTransformer,
                                                 SchemaGeneratorHelper schemaHelper,
                                                 MethodResponseExtractor methodResponseExtractor) {
        super(globalExceptionHandlerClasses, dataTypeTransformer, schemaHelper, methodResponseExtractor);
    }

    @Override
    protected Class<?> getExceptionMapperClass() {
        return ExceptionMapper.class;
    }

    @Override
    protected Class<?> getResponseClass() {
        return Response.class;
    }

    @Override
    protected Class<?> getResponseStatusClass() {
        return Response.Status.class;
    }

}
