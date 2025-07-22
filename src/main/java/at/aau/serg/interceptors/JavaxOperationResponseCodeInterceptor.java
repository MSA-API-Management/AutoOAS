package at.aau.serg.interceptors;

import com.github.jrcodeza.schema.generator.DataTypeTransformer;
import com.github.jrcodeza.schema.generator.MethodResponseExtractor;
import com.github.jrcodeza.schema.generator.util.SchemaGeneratorHelper;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtType;
import spoon.support.reflect.code.CtFieldReadImpl;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.List;

public class JavaxOperationResponseCodeInterceptor extends AbstractJaxRsOperationResponseCodeInterceptor {

    public JavaxOperationResponseCodeInterceptor(List<CtType<?>> globalExceptionHandlerClasses,
                                                 DataTypeTransformer dataTypeTransformer,
                                                 SchemaGeneratorHelper schemaHelper,
                                                 MethodResponseExtractor methodResponseExtractor) {
        super(globalExceptionHandlerClasses, dataTypeTransformer, schemaHelper, methodResponseExtractor);
    }

    @Override
    protected List<Integer> getResponseCodesFromResponseBuilderMethod(CtInvocation<?> method) {
        String methodName = method.getExecutable().getSimpleName();
        List<Integer> responseStatus = null;

        // handle response code
        switch (methodName) {
            case "ok" -> responseStatus = List.of(Response.Status.OK.getStatusCode());
            case "noContent" -> responseStatus = List.of(Response.Status.NO_CONTENT.getStatusCode());
            case "accepted" -> responseStatus = List.of(Response.Status.ACCEPTED.getStatusCode());
            case "notModified" -> responseStatus = List.of(Response.Status.NOT_MODIFIED.getStatusCode());
            case "created" -> responseStatus = List.of(Response.Status.CREATED.getStatusCode());
            case "serverError" -> responseStatus = List.of(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            case "status" -> {
                // custom statusCode with either Response.StatusType or int (::status overload)
                var statusCodeMethodArg = method.getArguments().getFirst();
                if (schemaHelper.isTypeEquivalent(statusCodeMethodArg.getType(), Response.StatusType.class)) {
                    int statusCode = Response.Status.valueOf(((CtFieldReadImpl<?>) statusCodeMethodArg).getVariable().getSimpleName()).getStatusCode();
                    responseStatus = List.of(statusCode);

                } else if ((schemaHelper.isTypeEquivalent(statusCodeMethodArg.getType(), Integer.class)
                        || (statusCodeMethodArg.getType() != null && statusCodeMethodArg.getType().getSimpleName().equals("int")))
                        && statusCodeMethodArg instanceof CtLiteral<?> literal) {
                    responseStatus = List.of((int) literal.getValue());

                } else {
                    responseStatus = tryExtractCommonResponseCodes(statusCodeMethodArg);
                }
            }
            default -> System.out.println("Unrecognized response builder status code method: " + methodName);
        }

        return responseStatus;
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
