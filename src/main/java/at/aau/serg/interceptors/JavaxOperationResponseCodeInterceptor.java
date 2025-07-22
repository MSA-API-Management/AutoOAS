package at.aau.serg.interceptors;

import com.github.jrcodeza.schema.generator.DataTypeTransformer;
import com.github.jrcodeza.schema.generator.MethodResponseExtractor;
import com.github.jrcodeza.schema.generator.util.SchemaGeneratorHelper;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtType;
import spoon.support.reflect.code.CtFieldReadImpl;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    // TODO make it cleaner & extract to use it in jakarta
    private List<Integer> tryExtractCommonResponseCodes(CtExpression<?> statusCodeMethodArg) {
        List<Integer> responseCodes = new ArrayList<>();
        if (statusCodeMethodArg instanceof CtConditional<?> ctConditional) {
            Integer thenCondition = extractSingleResponseCode(ctConditional.getThenExpression());
            if (thenCondition != null) {
                responseCodes.add(thenCondition);
            }

            Integer elseCondition = extractSingleResponseCode(ctConditional.getElseExpression());
            if (elseCondition != null) {
                responseCodes.add(elseCondition);
            }
        } else {
            Integer singleCode = extractSingleResponseCode(statusCodeMethodArg);
            if (singleCode != null) {
                responseCodes.add(singleCode);
            }
        }

        return responseCodes;
    }

    // TODO extract if we are sure to use HttpStatus
    private Integer extractSingleResponseCode(CtExpression<?> expression) {
        Map<String, HttpStatus> statusMap = Map.of(
                "BAD_REQUEST", HttpStatus.BAD_REQUEST,
                "NOT_FOUND", HttpStatus.NOT_FOUND,
                "NO_CONTENT", HttpStatus.NO_CONTENT,
                "ACCEPTED", HttpStatus.ACCEPTED,
                "PARTIAL_CONTENT", HttpStatus.PARTIAL_CONTENT,
                "CREATED", HttpStatus.CREATED,
                "FAILURE", HttpStatus.METHOD_FAILURE
        );

        return statusMap.entrySet().stream()
                .filter(entry -> expression.toString().contains(entry.getKey()))
                .map(entry -> entry.getValue().value())
                .findFirst()
                .orElseGet(() -> {
                    System.out.println("Could not parse custom response code creation in builder::status: " + expression);
                    return null;
                });
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
