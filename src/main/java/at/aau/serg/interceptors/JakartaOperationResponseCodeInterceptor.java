package at.aau.serg.interceptors;

import com.github.jrcodeza.schema.generator.DataTypeTransformer;
import com.github.jrcodeza.schema.generator.MethodResponseExtractor;
import com.github.jrcodeza.schema.generator.util.SchemaGeneratorHelper;
import io.swagger.v3.oas.models.responses.ApiResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.springframework.http.HttpStatus;
import spoon.reflect.code.CtExpression;
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
        }

        return responseStatus;
    }

    private List<Integer> tryExtractCommonResponseCodes(CtExpression<?> statusCodeMethodArg) {
        if (statusCodeMethodArg.toString().contains("BAD_REQUEST")) {
            return List.of(HttpStatus.BAD_REQUEST.value());
        }

        // todo detect more common response code logic - especially apache HttpStatus
        System.out.println("Could not parse custom response code creation in builder::status: " + statusCodeMethodArg);

        return null;
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
