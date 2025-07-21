package at.aau.serg.interceptors;

import com.github.jrcodeza.schema.generator.DataTypeTransformer;
import com.github.jrcodeza.schema.generator.MethodResponseExtractor;
import com.github.jrcodeza.schema.generator.util.SchemaGeneratorHelper;
import io.swagger.v3.oas.models.responses.ApiResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtType;
import spoon.support.reflect.code.CtFieldReadImpl;

import java.util.List;

public class JakartaOperationResponseCodeInterceptor extends AbstractJaxRsOperationResponseCodeInterceptor {

    public JakartaOperationResponseCodeInterceptor(List<CtType<?>> globalExceptionHandlerClasses,
                                                   DataTypeTransformer dataTypeTransformer,
                                                   SchemaGeneratorHelper schemaHelper,
                                                   MethodResponseExtractor methodResponseExtractor) {
        super(globalExceptionHandlerClasses, dataTypeTransformer, schemaHelper, methodResponseExtractor);
    }

    @Override
    protected Integer getResponseCodeFromResponseBuilderMethod(CtInvocation<?> method) {
        String methodName = method.getExecutable().getSimpleName();
        Integer responseStatus = null;

        // handle response code
        switch (methodName) {
            case "ok" -> responseStatus = Response.Status.OK.getStatusCode();
            case "noContent" -> responseStatus = Response.Status.NO_CONTENT.getStatusCode();
            case "accepted" -> responseStatus = Response.Status.ACCEPTED.getStatusCode();
            case "notModified" -> responseStatus = Response.Status.NOT_MODIFIED.getStatusCode();
            case "created" -> responseStatus = Response.Status.CREATED.getStatusCode();
            case "status" -> {
                // custom statusCode with either Response.StatusType or int (::status overload)
                var statusCodeMethodArg = method.getArguments().getFirst();
                if (schemaHelper.isTypeEquivalent(statusCodeMethodArg.getType(), Response.StatusType.class)) {
                    responseStatus = Response.Status.valueOf(((CtFieldReadImpl<?>) statusCodeMethodArg).getVariable().getSimpleName()).getStatusCode();

                } else if ((schemaHelper.isTypeEquivalent(statusCodeMethodArg.getType(), Integer.class)
                        || statusCodeMethodArg.getType().getSimpleName().equals("int"))
                        && statusCodeMethodArg instanceof CtLiteral<?> literal) {
                    responseStatus = (int) literal.getValue();
                }
            }
        }
        return responseStatus;
    }

    @Override
    protected void setResponseDescription(ApiResponse response, Integer responseStatus) {
        response.setDescription(Response.Status.fromStatusCode(responseStatus) != null ? Response.Status.fromStatusCode(responseStatus).getReasonPhrase() : "");
    }

    @Override
    protected Class<?> getExceptionMapperClass() {
        return ExceptionMapper.class;
    }

    @Override
    protected Class<?> getResponseClass() {
        return Response.class;
    }

}
