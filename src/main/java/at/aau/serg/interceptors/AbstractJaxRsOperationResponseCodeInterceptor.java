package at.aau.serg.interceptors;

import com.github.jrcodeza.schema.generator.DataTypeTransformer;
import com.github.jrcodeza.schema.generator.MethodResponseExtractor;
import com.github.jrcodeza.schema.generator.interceptors.OperationInterceptor;
import com.github.jrcodeza.schema.generator.util.SchemaGeneratorHelper;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.apache.commons.lang3.NotImplementedException;
import org.javatuples.Pair;
import org.springframework.http.HttpStatus;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractJaxRsOperationResponseCodeInterceptor implements OperationInterceptor {

    protected static final List<String> KNOWN_AND_IGNORED_RESPONSE_BUILDER_METHODS = List.of("lastModified", "tag", "entity");

    protected static final int FALLBACK_STATUS_CODE = 200;

    protected List<CtType<?>> globalExceptionHandlerClasses; // todo check for equivalent of controllerAdviceClasses

    protected DataTypeTransformer dataTypeTransformer;
    protected SchemaGeneratorHelper schemaHelper;
    protected MethodResponseExtractor methodResponseExtractor;

    private static final Map<String, HttpStatus> EXCEPTION_STATUS_MAP = Map.of(
            "BadRequestException", HttpStatus.BAD_REQUEST,
            "ForbiddenException", HttpStatus.FORBIDDEN,
            "NotAcceptableException", HttpStatus.NOT_ACCEPTABLE,
            "NotAllowedException", HttpStatus.METHOD_NOT_ALLOWED,
            "NotAuthorizedException", HttpStatus.UNAUTHORIZED,
            "NotFoundException", HttpStatus.NOT_FOUND,
            "NotSupportedException", HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "InternalServerErrorException", HttpStatus.INTERNAL_SERVER_ERROR,
            "ServiceUnavailableException", HttpStatus.SERVICE_UNAVAILABLE
    );

    private static final Map<String, HttpStatus> HTTP_STATUS_CONSTANTS = Map.of(
            "BAD_REQUEST", HttpStatus.BAD_REQUEST,
            "NOT_FOUND", HttpStatus.NOT_FOUND,
            "NO_CONTENT", HttpStatus.NO_CONTENT,
            "ACCEPTED", HttpStatus.ACCEPTED,
            "PARTIAL_CONTENT", HttpStatus.PARTIAL_CONTENT,
            "CREATED", HttpStatus.CREATED,
            "FAILURE", HttpStatus.METHOD_FAILURE
            // TODO extend if necessary
    );


    public AbstractJaxRsOperationResponseCodeInterceptor(List<CtType<?>> globalExceptionHandlerClasses,
                                                         DataTypeTransformer dataTypeTransformer,
                                                         SchemaGeneratorHelper schemaHelper,
                                                         MethodResponseExtractor methodResponseExtractor) {
        this.globalExceptionHandlerClasses = globalExceptionHandlerClasses;
        this.dataTypeTransformer = dataTypeTransformer;
        this.schemaHelper = schemaHelper;
        this.methodResponseExtractor = methodResponseExtractor;
    }

    @Override
    public void intercept(Method method, Operation transformedOperation) {
        throw new NotImplementedException();
    }

    @Override
    public void intercept(CtMethod<?> method, Operation transformedOperation) {

        //// Response detection ////
        var responses = tryDetectJaxRSResponsesInMethod(method);

        if (!responses.isEmpty()) {
            // found some Jax-RS Response obj, overwrite og responses
            transformedOperation.setResponses(responses);
        }
        // else: keep the original responses, assuming the method has another return type than jakarta.ws.rs.core.Response or javax.ws.rs.core.Response

        //// Exception Detection ////
        var exceptionResponses = tryDetectExceptionsInMethod(method);

        if (!exceptionResponses.isEmpty()) {
            boolean hasRegularReturnStatements = !method.getBody().getElements(new TypeFilter<>(CtReturn.class)).isEmpty();

            if (hasRegularReturnStatements) {
                // append exceptions to regular responses
                transformedOperation.getResponses().putAll(exceptionResponses);
            } else {
                // overwrite any non-error default responses
                transformedOperation.setResponses(exceptionResponses);
            }
        }

    }

// region Response obj detection

    /**
     * Detects returned jakarta.ws.rs.core.Response and javax.ws.rs.core.Response objects in Jax-RS handler methods.
     *
     * @param method
     * @return
     */
    private ApiResponses tryDetectJaxRSResponsesInMethod(CtMethod<?> method) {
        ApiResponses apiResponses = new ApiResponses();

        // fixme limitation: only handle direct invocation at return statement
        for (var returnStatement : method.getElements(new TypeFilter<>(CtReturn.class))) {
            CtExpression<?> returned = returnStatement.getReturnedExpression();
            if (returned instanceof CtInvocation<?> inv) {

                var responses = analyzeResponseInvocation(inv);

                if (responses != null && !responses.isEmpty()) {
                    for (var response : responses) {
                        apiResponses.addApiResponse(response.getValue0(), response.getValue1());
                    }
                }
            }
        }

        return apiResponses;
    }

    /**
     * Creates the ApiResponse starting from the build() call of a Jax-RS Response object.
     *
     * @param inv
     * @return
     */
    private List<Pair<String, ApiResponse>> analyzeResponseInvocation(CtInvocation<?> inv) {
        CtExecutableReference<?> executable = inv.getExecutable();
        String methodName = executable.getSimpleName();

        if (methodName.equals("build")) {
            // detect builder call
            if (inv.getTarget() instanceof CtInvocation<?> baseInvocation) {
                return traceResponseCreationBackFromBuildCall(baseInvocation);

            } else {
                // eg., builder.build()
                // todo support this ^
            }
        }

        return null;
    }

    /**
     * Backtracking method chains, e.g.,
     * Response
     * .status(Response.Status.UNAUTHORIZED)
     * .entity(new Simple().withName("Peter").withId(1))
     * .build();
     *
     * @param buildCallTarget starting before the build call, in the example from ::entity
     * @return a list of response codes and response type pairs
     */
    private List<Pair<String, ApiResponse>> traceResponseCreationBackFromBuildCall(CtInvocation<?> buildCallTarget) {
        List<Integer> responseStatus = new ArrayList<>();
        ApiResponse baseResponseSchema = new ApiResponse();

        CtExpression<?> curMethodInChain = buildCallTarget;
        while (curMethodInChain instanceof CtInvocation<?> method) {
            String methodName = method.getExecutable().getSimpleName();

            // handle response schema
            if (methodName.equals("ok") || methodName.equals("entity")) {
                List<CtExpression<?>> args = method.getArguments();
                if (!args.isEmpty()) {
                    CtExpression<?> arg = args.getFirst();
                    baseResponseSchema = extractPayloadTypeInfo(arg);
                }
            }

            // handle response code
            List<Integer> methodResponseStatus = tryGetResponseCodesFromResponseBuilderMethod(method);
            if (methodResponseStatus != null && !methodResponseStatus.isEmpty()) {
                responseStatus.addAll(methodResponseStatus);
            }

            curMethodInChain = method.getTarget();
        }

        if (responseStatus.isEmpty()) {
            responseStatus = List.of(FALLBACK_STATUS_CODE); // fallback!
        }

        List<Pair<String, ApiResponse>> responses = new ArrayList<>();
        for (Integer status : responseStatus) {
            ApiResponse clonedResponse = cloneApiResponse(baseResponseSchema);
            setResponseDescription(clonedResponse, status);
            responses.add(new Pair<>(String.valueOf(status), clonedResponse));
        }

        return responses;
    }


    private ApiResponse cloneApiResponse(ApiResponse original) {
        if (original == null) {
            return new ApiResponse();
        }

        ApiResponse clone = new ApiResponse();

        clone.setDescription(original.getDescription());
        clone.setContent(original.getContent());
        clone.setHeaders(original.getHeaders());
        clone.setLinks(original.getLinks());
        clone.setExtensions(original.getExtensions());

        return clone;
    }

    private void setResponseDescription(ApiResponse response, Integer responseStatus) {
        response.setDescription(HttpStatus.valueOf(responseStatus).getReasonPhrase());
    }

    /**
     * Detects common methods of the Response builder pattern that set a response code, e.g., {@code ok(.)}, {@code noContent()},
     * and returns the corresponding response code if detected.
     *
     * @param method
     * @return
     */
    abstract protected List<Integer> tryGetResponseCodesFromResponseBuilderMethod(CtInvocation<?> method);

    protected List<Integer> tryExtractCommonResponseCodes(CtExpression<?> statusCodeMethodArg) {
        List<Integer> responseCodes = new ArrayList<>();
        if (statusCodeMethodArg instanceof CtConditional<?> ctConditional) {
            responseCodes.addAll(tryExtractCommonResponseCodes(ctConditional.getThenExpression()));
            responseCodes.addAll(tryExtractCommonResponseCodes(ctConditional.getElseExpression()));
        } else {
            Integer singleCode = extractSingleResponseCode(statusCodeMethodArg);
            if (singleCode != null) {
                responseCodes.add(singleCode);
            }
        }

        return responseCodes;
    }

    /**
     * Extracts HTTP status code e.g. from Apache HttpStatus constant expressions.
     * Maps e.g. Apache constants (e.g., {@code SC_BAD_REQUEST}) to Spring HttpStatus values.
     *
     * @param expression code expression containing HttpStatus constant
     * @return HTTP status code (e.g., 400, 404) or {@code null} if not found
     * @example {@code "org.apache.http.HttpStatus.SC_BAD_REQUEST" → 400}
     */
    protected Integer extractSingleResponseCode(CtExpression<?> expression) {
        return extractStatusCode(expression.toString(), HTTP_STATUS_CONSTANTS, "response code creation in builder::status");
    }

    private Integer extractStatusCode(String input, Map<String, HttpStatus> statusMap, String context) {
        return statusMap.entrySet().stream()
                .filter(entry -> input.contains(entry.getKey()))
                .map(entry -> entry.getValue().value())
                .findFirst()
                .orElseGet(() -> {
                    System.out.println("Could not parse status code for " + context + ": " + input);
                    return null;
                });
    }


    private ApiResponse extractPayloadTypeInfo(CtExpression<?> expr) {
        ApiResponse response = null;

        // Case 1: Variable read (e.g., 'al')
        if (expr instanceof CtVariableRead<?> varRead) {
            CtVariable<?> varDecl = varRead.getVariable().getDeclaration();
            if (varDecl != null) {
                CtTypeReference<?> type = varDecl.getType();

                response = dataTypeTransformer.detectAndCreateApiResponseContent(type);
            }
        }

        // Case 2: Factory call (e.g., List.of(...))
        else if (expr instanceof CtInvocation<?> call && isListFactoryCall(call)) {
            response = dataTypeTransformer.detectAndCreateApiResponseContent(call.getType());
        }

        // Case 3: conditional (e.g., b ? a : b)
        else if (expr instanceof CtConditional<?>) {
            response = dataTypeTransformer.detectAndCreateApiResponseContent(((CtConditional<?>) expr).getThenExpression().getType()); // use any of then, else as they have to be the same type
        }

        // Fallback: direct object
        else {
            response = dataTypeTransformer.detectAndCreateApiResponseContent(expr.getType());
        }

        return response;
    }

// endregion Response obj detection

// region Exception detection

    private ApiResponses tryDetectExceptionsInMethod(CtMethod<?> method) {
        ApiResponses apiResponses = new ApiResponses();

        for (var throwsStatement : method.getElements(new TypeFilter<>(CtThrow.class))) {
            CtType<?> thrownType = throwsStatement.getThrownExpression().getType().getTypeDeclaration();

            if (thrownType == null) {
                // todo resolve or refactor to TypeReference
                System.out.println("Cannot resolve CtTypeReference: " + throwsStatement.getThrownExpression().getType().toString());
            } else {
                ApiResponses resolvedResponse = resolveExceptionResponse(thrownType);
                apiResponses.putAll(resolvedResponse);
            }
        }

        return apiResponses;
    }

    private ApiResponses resolveExceptionResponse(CtType<?> thrownType) {
        // todo david check for local exception handling
        // Global Exception Handles
        ApiResponses response = tryResolveStatusCodeFromGlobalExceptionHandlers(thrownType);
        if (response != null) return response;

        // Direct exception mapping (throws)
        response = tryResolveStatusCodeFromThrownException(thrownType);
        if (response != null) return response;

        // 500 Fallback
        return createInternalServerErrorResponse();
    }


    // TODO should be moved to dedicated analysis
    private Map<CtType<?>, ApiResponses> cachedExceptionApiResponsesMapping = new HashMap();

    private ApiResponses tryResolveStatusCodeFromGlobalExceptionHandlers(CtType<?> thrownType) {
        if (cachedExceptionApiResponsesMapping.containsKey(thrownType)) {
            return cachedExceptionApiResponsesMapping.get(thrownType);
        }

        ApiResponses apiResponses = null;
        boolean globalExceptionHandlerFound = false;

        for (CtType<?> globalExceptionHandler : globalExceptionHandlerClasses) {
            // Jax-RS requires annotation and interface impl, just confirming we extracted correctly
            assert schemaHelper.isTypeEquivalent(globalExceptionHandler.getReference(), getExceptionMapperClass());

            var exceptionHandlerMethod = globalExceptionHandler.getMethod("toResponse", thrownType.getReference());
            if (exceptionHandlerMethod != null) {

                var exceptionHandlerResponseType = exceptionHandlerMethod.getType();
                if (schemaHelper.isTypeEquivalent(exceptionHandlerResponseType, getResponseClass())) {
                    // returning Jax-RS Response -> extract actual response info
                    apiResponses = tryDetectJaxRSResponsesInMethod(exceptionHandlerMethod);
                } else {
                    // returning pojo
                    apiResponses = methodResponseExtractor.createApiResponses(exceptionHandlerMethod, null);
                }

                // todo david whats happening in production if there are multiple global exception handlers?
                cachedExceptionApiResponsesMapping.put(thrownType, apiResponses);
                break;
            }
        }

        return apiResponses;
    }

    private ApiResponses tryResolveStatusCodeFromThrownException(CtType<?> thrownType) {
        String exceptionName = thrownType.getSimpleName();
        Integer statusCode = extractStatusCode(exceptionName, EXCEPTION_STATUS_MAP, "throw exception");
        return statusCode != null ? createApiResponse(statusCode) : null;
    }

    private ApiResponses createApiResponse(Integer statusCode) {
        HttpStatus status = HttpStatus.valueOf(statusCode);
        ApiResponses apiResponses = new ApiResponses();
        return apiResponses.addApiResponse(
                String.valueOf(status.value()),
                new ApiResponse().description(status.getReasonPhrase())
        );
    }

    private ApiResponses createInternalServerErrorResponse() {
        ApiResponses apiResponses = new ApiResponses();
        apiResponses.addApiResponse("500", new ApiResponse().description("Internal Server Error"));
        return apiResponses;
    }

    abstract protected Class<?> getExceptionMapperClass();

    abstract protected Class<?> getResponseClass();


// endregion Exception detection

// region helpers

    boolean isListFactoryCall(CtInvocation<?> inv) {
        String methodName = inv.getExecutable().getSimpleName();
        String declaringType = inv.getExecutable().getDeclaringType().getQualifiedName();
        return (declaringType.equals("java.util.List") || declaringType.equals("java.util.Arrays")) && (methodName.equals("of") || methodName.equals("asList"));
    }

// endregion helpers

}
