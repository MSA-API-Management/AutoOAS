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

    protected static final int FALLBACK_STATUS_CODE = 200;

    protected List<CtType<?>> globalExceptionHandlerClasses; // todo check for equivalent of controllerAdviceClasses

    protected DataTypeTransformer dataTypeTransformer;
    protected SchemaGeneratorHelper schemaHelper;
    protected MethodResponseExtractor methodResponseExtractor;


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
        ApiResponse baseResponse = new ApiResponse();

        CtExpression<?> curMethodInChain = buildCallTarget;
        while (curMethodInChain instanceof CtInvocation<?> method) {
            String methodName = method.getExecutable().getSimpleName();

            // handle response type
            if (methodName.equals("ok") || methodName.equals("entity")) {
                List<CtExpression<?>> args = method.getArguments();
                if (!args.isEmpty()) {
                    CtExpression<?> arg = args.getFirst();
                    baseResponse = extractPayloadTypeInfo(arg);
                }
            }

            List<Integer> methodResponseStatus = getResponseCodesFromResponseBuilderMethod(method);
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
            ApiResponse clonedResponse = cloneApiResponse(baseResponse);
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

    abstract protected List<Integer> getResponseCodesFromResponseBuilderMethod(CtInvocation<?> method);


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
            ApiResponses apiResponsesForCurrentThrows = null;

            // todo david check for local exception handling

            // global exception handling
            if (apiResponsesForCurrentThrows == null) {
                apiResponsesForCurrentThrows = tryResolveStatusCodeFromGlobalExceptionHandlers(thrownType);
            }

            // 500 fallback, because no handler was found
            if (apiResponsesForCurrentThrows == null) {
                apiResponsesForCurrentThrows = new ApiResponses();
                apiResponsesForCurrentThrows.addApiResponse("500", new ApiResponse().description("Internal Server Error"));
            }

            apiResponses.putAll(apiResponsesForCurrentThrows);
        }

        return apiResponses;
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
