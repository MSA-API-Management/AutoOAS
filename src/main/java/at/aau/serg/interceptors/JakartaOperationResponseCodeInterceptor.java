package at.aau.serg.interceptors;

import com.github.jrcodeza.schema.generator.DataTypeTransformer;
import com.github.jrcodeza.schema.generator.MethodResponseExtractor;
import com.github.jrcodeza.schema.generator.util.SchemaGeneratorHelper;
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
    protected List<Integer> tryGetResponseCodesFromResponseBuilderMethod(CtInvocation<?> method) {
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
            case "temporaryRedirect" -> responseStatus = List.of(Response.Status.TEMPORARY_REDIRECT.getStatusCode());

            // chatgpt recommended, not all exist in javax but lets keep them for sync with jakarta impl
            case "badRequest" -> responseStatus = List.of(Response.Status.BAD_REQUEST.getStatusCode());
            case "notFound" -> responseStatus = List.of(Response.Status.NOT_FOUND.getStatusCode());
            case "unauthorized" -> responseStatus = List.of(Response.Status.UNAUTHORIZED.getStatusCode());
            case "forbidden" -> responseStatus = List.of(Response.Status.FORBIDDEN.getStatusCode());
            case "conflict" -> responseStatus = List.of(Response.Status.CONFLICT.getStatusCode());
            case "notAcceptable" -> responseStatus = List.of(Response.Status.NOT_ACCEPTABLE.getStatusCode());

            // manual status detection
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

            // ignore or log others
            default -> {
                if (!KNOWN_AND_IGNORED_RESPONSE_BUILDER_METHODS.contains(methodName))
                    System.out.println("Unrecognized response builder status code method: " + methodName);
            }
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
