package at.aau.serg.interceptors;

import com.github.jrcodeza.schema.generator.DataTypeTransformer;
import com.github.jrcodeza.schema.generator.MethodResponseExtractor;
import com.github.jrcodeza.schema.generator.util.SchemaGeneratorHelper;
import io.swagger.v3.oas.models.responses.ApiResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtType;
import spoon.support.reflect.code.CtFieldReadImpl;

import java.util.List;

public class JavaxOperationResponseCodeInterceptor extends AbstractJaxRsOperationResponseCodeInterceptor {

    public JavaxOperationResponseCodeInterceptor(List<CtType<?>> globalExceptionHandlerClasses,
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
        if (methodName.equals("ok")) {
            responseStatus = Response.Status.OK.getStatusCode();
        } else if (methodName.equals("noContent")) {
            responseStatus = Response.Status.NO_CONTENT.getStatusCode();
        } else if (methodName.equals("accepted")) {
            responseStatus = Response.Status.ACCEPTED.getStatusCode();
        } else if (methodName.equals("notModified")) {
            responseStatus = Response.Status.NOT_MODIFIED.getStatusCode();
        } else if (methodName.equals("created")) {
            responseStatus = Response.Status.CREATED.getStatusCode();
        } else if (methodName.equals("status")) {
            // custom statusCode with either Response.StatusType or int (::status overload)
            var statusCodeMethodArg = method.getArguments().getFirst();
            if (schemaHelper.isTypeEquivalent(statusCodeMethodArg.getType(), Response.StatusType.class)) {
                responseStatus = Response.Status.valueOf(((CtFieldReadImpl<?>) statusCodeMethodArg).getVariable().getSimpleName()).getStatusCode();

            } else if ((schemaHelper.isTypeEquivalent(statusCodeMethodArg.getType(), Integer.class)
                    || (statusCodeMethodArg.getType() != null && statusCodeMethodArg.getType().getSimpleName().equals("int")))
                    && statusCodeMethodArg instanceof CtLiteral<?> literal) {
                responseStatus = (int) literal.getValue();

            } else {
                responseStatus = tryExtractCommonResponseCodes(statusCodeMethodArg);
            }
        }

        return responseStatus;
    }

    private Integer tryExtractCommonResponseCodes(CtExpression<?> statusCodeMethodArg) {
        if (statusCodeMethodArg.toString().contains("BAD_REQUEST")) {
            return Response.Status.BAD_REQUEST.getStatusCode();
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
