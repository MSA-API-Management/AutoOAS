package at.aau.serg.interceptors;

import com.github.jrcodeza.schema.generator.DataTypeTransformer;
import com.github.jrcodeza.schema.generator.MethodResponseExtractor;
import com.github.jrcodeza.schema.generator.util.SchemaGeneratorHelper;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.util.List;

public class JakartaOperationResponseCodeInterceptor extends AbstractJaxRsOperationResponseCodeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JakartaOperationResponseCodeInterceptor.class);

    public JakartaOperationResponseCodeInterceptor(List<CtType<?>> globalExceptionHandlerClasses,
                                                   DataTypeTransformer dataTypeTransformer,
                                                   SchemaGeneratorHelper schemaHelper,
                                                   MethodResponseExtractor methodResponseExtractor,
                                                   boolean exceptionLoggingEnabled) {
        super(globalExceptionHandlerClasses, dataTypeTransformer, schemaHelper, methodResponseExtractor, exceptionLoggingEnabled);
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
