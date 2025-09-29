package at.aau.serg.interceptors;

import at.aau.serg.codeanalysis.MethodBodyAnalyser;
import at.aau.serg.util.SpoonUtils;
import com.github.jrcodeza.schema.generator.DataTypeTransformer;
import com.github.jrcodeza.schema.generator.MethodResponseExtractor;
import com.github.jrcodeza.schema.generator.interceptors.OperationInterceptor;
import com.github.jrcodeza.schema.generator.util.SchemaGeneratorHelper;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import lombok.Setter;
import org.apache.commons.lang3.NotImplementedException;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.lang.reflect.Method;
import java.util.*;

import static at.aau.serg.util.SpoonUtils.getMethodDeclaration;

public abstract class AbstractJaxRsOperationResponseCodeInterceptor implements OperationInterceptor {
    @Setter
    private int maxCrossClassDepth = 1; // Analyze current class + X level hierarchically deeper classes (-1 for unlimited analysis)

    protected static final List<String> KNOWN_AND_IGNORED_RESPONSE_BUILDER_METHODS = List.of("lastModified", "tag", "entity");

    protected static final HttpStatus FALLBACK_STATUS_CODE = HttpStatus.OK;

    private static final Logger logger = LoggerFactory.getLogger(AbstractJaxRsOperationResponseCodeInterceptor.class);
    private static final MethodBodyAnalyser methodBodyAnalyser = new MethodBodyAnalyser();
    private final Map<CtMethod<?>, Set<CtType<?>>> methodExceptionCache = new HashMap<>();
    private String operationPath = "";

    protected List<CtType<?>> globalExceptionHandlerClasses; // todo check for equivalent of controllerAdviceClasses

    protected DataTypeTransformer dataTypeTransformer;
    protected SchemaGeneratorHelper schemaHelper;
    protected MethodResponseExtractor methodResponseExtractor;

    private static final Map<String, HttpStatus> EXCEPTION_STATUS_MAP = Map.of(
            "BadRequestException", HttpStatus.BAD_REQUEST,
            "NotAuthorizedException", HttpStatus.UNAUTHORIZED,
            "ForbiddenException", HttpStatus.FORBIDDEN,
            "ForbiddenAccessException", HttpStatus.FORBIDDEN,
            "NotFoundException", HttpStatus.NOT_FOUND,
            "NotAllowedException", HttpStatus.METHOD_NOT_ALLOWED,
            "NotAcceptableException", HttpStatus.NOT_ACCEPTABLE,
            "NotSupportedException", HttpStatus.UNSUPPORTED_MEDIA_TYPE,

            "InternalServerErrorException", HttpStatus.INTERNAL_SERVER_ERROR,
            "ServiceUnavailableException", HttpStatus.SERVICE_UNAVAILABLE
    );

    private static final Map<String, HttpStatus> HTTP_STATUS_CONSTANTS = Map.ofEntries(
            Map.entry("OK", HttpStatus.OK),
            Map.entry("CREATED", HttpStatus.CREATED),
            Map.entry("ACCEPTED", HttpStatus.ACCEPTED),
            Map.entry("NO_CONTENT", HttpStatus.NO_CONTENT),
            Map.entry("PARTIAL_CONTENT", HttpStatus.PARTIAL_CONTENT),

            Map.entry("BAD_REQUEST", HttpStatus.BAD_REQUEST),
            Map.entry("UNAUTHORIZED", HttpStatus.UNAUTHORIZED),
            Map.entry("FORBIDDEN", HttpStatus.FORBIDDEN),
            Map.entry("NOT_FOUND", HttpStatus.NOT_FOUND),
            Map.entry("METHOD_NOT_ALLOWED", HttpStatus.METHOD_NOT_ALLOWED),
            Map.entry("NOT_ACCEPTABLE", HttpStatus.NOT_ACCEPTABLE),
            Map.entry("UNSUPPORTED_MEDIA_TYPE", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
            Map.entry("FAILURE", HttpStatus.METHOD_FAILURE),

            Map.entry("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR),
            Map.entry("SERVICE_UNAVAILABLE", HttpStatus.SERVICE_UNAVAILABLE)
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
        this.operationPath = (String) transformedOperation.getExtensions().get("x-path");

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
            if (hasRegularReturns(method)) {
                // append exceptions to regular responses
                transformedOperation.getResponses().putAll(exceptionResponses);
            } else {
                // overwrite any non-error default responses
                transformedOperation.setResponses(exceptionResponses);
            }
        }

    }

    private boolean hasRegularReturns(CtMethod<?> method) {
        // todo check
        boolean hasExplicitReturns = !method.getBody().getElements((CtReturn<?> r) -> r.getParent(CtLambda.class) == null).isEmpty();
        if (hasExplicitReturns)
            return true;

        if (method.getType().equals(method.getFactory().Type().voidPrimitiveType())) {
            // void method might have implicit returns
            return methodBodyAnalyser.mayCompleteNormallyWithoutEarlyCompletion(method);
        }

        return false;
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
        ApiResponses otherObjectApiResponses = new ApiResponses();

        boolean methodHasObjectReturnType = SpoonUtils.isObjectType(method.getType());

        // fixme limitation: only handle direct invocation at return statement
        for (var returnStatement : method.getElements(new TypeFilter<>(CtReturn.class))) {
            CtExpression<?> returned = returnStatement.getReturnedExpression();

            if (returned instanceof CtInvocation<?> inv
                    && SpoonUtils.isTypeEquivalent(inv.getType(), this.getResponseClass())) {
                // analyzing JAX-RS responses
                var responses = analyzeResponseInvocation(inv);

                if (responses != null && !responses.isEmpty()) {
                    for (var response : responses) {
                        apiResponses.addApiResponse(response.getValue0(), response.getValue1());
                    }
                }
            } else if (methodHasObjectReturnType &&
                    (returned instanceof CtVariableAccess<?> || returned instanceof CtInvocation<?>)) {
                // handling other returned types in methods returning generic Objects
                var apiResponse = dataTypeTransformer.detectAndCreateApiResponseContent(returned.getType());
                var status = FALLBACK_STATUS_CODE;
                apiResponse.setDescription(status.getReasonPhrase());
                otherObjectApiResponses.addApiResponse(String.valueOf(status.value()), apiResponse);
            }
        }

        if (!apiResponses.isEmpty()) {
            // found some returned Responses, consider also other returned objects
            apiResponses.putAll(otherObjectApiResponses);
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
        if (!SpoonUtils.isTypeEquivalent(inv.getType(), this.getResponseClass())) {
            return null;
        }

        CtExecutableReference<?> executable = inv.getExecutable();

        String methodName = executable.getSimpleName();
        if (methodName.equals("build")) {
            // detect Response builder call
            if (inv.getTarget() instanceof CtInvocation<?> baseInvocation) {
                return traceResponseCreationBackFromBuildCall(baseInvocation);

            } else {
                // eg., builder.build()
                // todo support this ^
            }

        } else if (executable.getParameters().stream().anyMatch(p -> SpoonUtils.isTypeEquivalent(p, getResponseStatusClass()))) {
            // greedy match for a method call parameter indicating a Response Status
            return tryExtractResponseStatusParameterFromMethodCall(inv);
        }

        return null;
    }

    /**
     * Extracts a Response.Status parameter provided to a method call, e.g., createCustomResponse(Response.Status.OK) -> 200.
     *
     * @param inv
     * @return
     */
    private List<Pair<String, ApiResponse>> tryExtractResponseStatusParameterFromMethodCall(CtInvocation<?> inv) {
        HttpStatus status = extractSingleResponseCode(inv);
        ApiResponse response = extractPayloadTypeInfo(inv);

        if (status != null && response != null) {
            response.setDescription(status.getReasonPhrase());
            return List.of(new Pair<>(String.valueOf(status.value()), response));

        } else
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
        List<HttpStatus> responseStatuses = new ArrayList<>();
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

            // handle response codes
            List<HttpStatus> methodResponseStatuses = tryGetResponseCodesFromResponseBuilderMethod(method);
            if (methodResponseStatuses != null && !methodResponseStatuses.isEmpty()) {
                responseStatuses.addAll(methodResponseStatuses);
            }

            curMethodInChain = method.getTarget();
        }

        if (responseStatuses.isEmpty()) {
            responseStatuses = List.of(FALLBACK_STATUS_CODE); // fallback!
        }

        List<Pair<String, ApiResponse>> responses = new ArrayList<>();
        for (HttpStatus status : responseStatuses) {
            ApiResponse clonedResponse = cloneApiResponse(baseResponseSchema);
            clonedResponse.setDescription(status.getReasonPhrase());
            responses.add(new Pair<>(String.valueOf(status.value()), clonedResponse));
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

    /**
     * Detects common methods of the Response builder pattern that set a response code, e.g., {@code ok(.)}, {@code noContent()},
     * and returns the corresponding response code if detected.
     *
     * @param method
     * @return
     */
    protected List<HttpStatus> tryGetResponseCodesFromResponseBuilderMethod(CtInvocation<?> method) {
        String methodName = method.getExecutable().getSimpleName();
        List<HttpStatus> responseStatuses = null;

        // handle response code
        switch (methodName) {
            case "ok" -> responseStatuses = List.of(HttpStatus.OK);
            case "noContent" -> responseStatuses = List.of(HttpStatus.NO_CONTENT);
            case "accepted" -> responseStatuses = List.of(HttpStatus.ACCEPTED);
            case "notModified" -> responseStatuses = List.of(HttpStatus.NOT_MODIFIED);
            case "created" -> responseStatuses = List.of(HttpStatus.CREATED);
            case "serverError" -> responseStatuses = List.of(HttpStatus.INTERNAL_SERVER_ERROR);
            case "temporaryRedirect" -> responseStatuses = List.of(HttpStatus.TEMPORARY_REDIRECT);
            case "seeOther" -> responseStatuses = List.of(HttpStatus.SEE_OTHER);

            // chatgpt recommended, not all exist in javax but lets keep them for sync with jakarta impl
            case "badRequest" -> responseStatuses = List.of(HttpStatus.BAD_REQUEST);
            case "notFound" -> responseStatuses = List.of(HttpStatus.NOT_FOUND);
            case "unauthorized" -> responseStatuses = List.of(HttpStatus.UNAUTHORIZED);
            case "forbidden" -> responseStatuses = List.of(HttpStatus.FORBIDDEN);
            case "conflict" -> responseStatuses = List.of(HttpStatus.CONFLICT);
            case "notAcceptable" -> responseStatuses = List.of(HttpStatus.NOT_ACCEPTABLE);

            // manual status detection
            case "status" -> {
                // custom statusCode with either Response.StatusType or int (::status overload)
                var statusCodeMethodArg = method.getArguments().getFirst();
                if (schemaHelper.isTypeEquivalent(statusCodeMethodArg.getType(), getResponseStatusClass())) {
                    if (statusCodeMethodArg instanceof CtFieldRead<?> fieldReadArg) {
                        HttpStatus statusCode = HttpStatus.valueOf(fieldReadArg.getVariable().getSimpleName());
                        responseStatuses = List.of(statusCode);
                    } else {
                        // todo handle method calls in status method, e.g., `Response.status(response.getStatusInfo())`
                        logger.error("Unrecognized response builder status method argument: {}", method.toStringDebug());
                    }

                } else if ((schemaHelper.isTypeEquivalent(statusCodeMethodArg.getType(), Integer.class)
                        || (statusCodeMethodArg.getType() != null && statusCodeMethodArg.getType().getSimpleName().equals("int")))
                        && statusCodeMethodArg instanceof CtLiteral<?> literal) {
                    responseStatuses = List.of(HttpStatus.valueOf((int) literal.getValue()));

                } else {
                    responseStatuses = tryExtractCommonResponseCodes(statusCodeMethodArg);
                }
            }

            // ignore or log others
            default -> {
                if (!KNOWN_AND_IGNORED_RESPONSE_BUILDER_METHODS.contains(methodName))
                    System.out.println("Unrecognized response builder status code method: " + methodName);
            }
        }

        return responseStatuses;
    }

    protected List<HttpStatus> tryExtractCommonResponseCodes(CtExpression<?> statusCodeMethodArg) {
        List<HttpStatus> responseCodes = new ArrayList<>();
        if (statusCodeMethodArg instanceof CtConditional<?> ctConditional) {
            responseCodes.addAll(tryExtractCommonResponseCodes(ctConditional.getThenExpression()));
            responseCodes.addAll(tryExtractCommonResponseCodes(ctConditional.getElseExpression()));
        } else {
            HttpStatus singleCode = extractSingleResponseCode(statusCodeMethodArg);
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
    protected HttpStatus extractSingleResponseCode(CtExpression<?> expression) {
        return extractStatusCode(expression.toString(), HTTP_STATUS_CONSTANTS, "response code creation");
    }

    private HttpStatus extractStatusCode(String input, Map<String, HttpStatus> statusMap, String context) {
        return statusMap.entrySet().stream()
                .filter(entry -> input.contains(entry.getKey()))
                .map(entry -> entry.getValue())
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

    /**
     * Detects and analyzes all exceptions that can be thrown by the given method,
     * resolving them into corresponding API response definitions with depth limitation.
     *
     * @param method the method to analyze for thrown exceptions
     * @return an ApiResponses object containing all resolved exception responses
     */
    private ApiResponses tryDetectExceptionsInMethod(CtMethod<?> method) {
        ApiResponses apiResponses = new ApiResponses();

        CtType<?> rootClass = method.getDeclaringType();
        Map<CtType<?>, Integer> classDepthMap = new HashMap<>();
        classDepthMap.put(rootClass, 0);

        Set<CtType<?>> allThrownExceptions = collectAllThrownExceptions(method, new HashSet<>(), classDepthMap, 0, new StringBuilder());

        for (CtType<?> thrownType : allThrownExceptions) {
            ApiResponses resolvedResponse = resolveExceptionResponse(thrownType);
            apiResponses.putAll(resolvedResponse);
        }

        return apiResponses;
    }

    /**
     * Recursively collects all exceptions that can be thrown by a method,
     * including direct throws, and exceptions from method calls.
     *
     * @param method            the method to analyze for thrown exceptions
     * @param visitedMethods    set of methods already visited to prevent infinite recursion
     * @param classDepthMap     map tracking the depth assigned to each class
     * @param currentClassDepth the depth of the current class being analyzed
     * @param callPath          the current call path as a StringBuilder
     * @return a set of all exception types that can be thrown by the method
     */
    private Set<CtType<?>> collectAllThrownExceptions(CtMethod<?> method, Set<CtMethod<?>> visitedMethods, Map<CtType<?>,Integer> classDepthMap, int currentClassDepth, StringBuilder callPath) {
        if (methodExceptionCache.containsKey(method)) {
            return methodExceptionCache.get(method);
        }

        if (visitedMethods.contains(method)) {
            return new HashSet<>();
        }
        visitedMethods.add(method);

        Set<CtType<?>> thrownExceptions = new HashSet<>();

        int originalLength = callPath.length();
        if (originalLength > 0)  callPath.append(" -> ");
        callPath.append(method.getSimpleName()).append("()");

        // direct throw statements
        for (var throwsStatement : method.getElements(new TypeFilter<>(CtThrow.class))) {
            CtType<?> thrownType = throwsStatement.getThrownExpression().getType().getTypeDeclaration();
            if (thrownType != null) {
                thrownExceptions.add(thrownType);
                logger.info("Exception Path {}: {} -> throw {}", this.operationPath, callPath, thrownType.getSimpleName());
            } else {
                logger.error("Cannot resolve type from CtTypeReference: {}", throwsStatement.getThrownExpression().getType().toString());
            }
        }

        // exceptions from method calls
        for (var invocation : method.getElements(new TypeFilter<>(CtInvocation.class))) {
            Set<CtType<?>> calledMethodExceptions = analyzeMethodCallExceptions(invocation, visitedMethods, classDepthMap, currentClassDepth, callPath);
            thrownExceptions.addAll(calledMethodExceptions);
        }

        methodExceptionCache.put(method, thrownExceptions);
        callPath.setLength(originalLength);

        return thrownExceptions;
    }

    /**
     * Analyzes a method invocation to collect all exceptions that can be thrown
     * by the called method, recursively following the call chain within depth limits.
     *
     * @param invocation        the method invocation to analyze
     * @param visitedMethods    set of methods already visited to prevent infinite recursion
     * @param classDepthMap     map tracking the depth assigned to each class
     * @param currentClassDepth the current class depth level
     * @param callPath          the current call path as a StringBuilder
     * @return a set of exception types that can be thrown by the invoked method
     */
    private Set<CtType<?>> analyzeMethodCallExceptions(CtInvocation<?> invocation, Set<CtMethod<?>> visitedMethods, Map<CtType<?>, Integer> classDepthMap, int currentClassDepth, StringBuilder callPath) {
        Set<CtType<?>> exceptions = new HashSet<>();

        try {
            CtExecutableReference<?> executableRef = invocation.getExecutable();

            // skip unresolvable methods or constructor
            if (executableRef == null || executableRef.getSimpleName().equals("<init>")) {
                return exceptions;
            }

            CtMethod<?> calledMethod = getMethodDeclaration(executableRef);

            if (calledMethod != null) {
                CtType<?> methodClass = calledMethod.getDeclaringType();

                int targetDepth = getOrAssignClassDepth(methodClass, classDepthMap, currentClassDepth);

                if (maxCrossClassDepth == -1 || targetDepth <= maxCrossClassDepth) {
                    Set<CtType<?>> calledMethodExceptions = collectAllThrownExceptions(calledMethod, new HashSet<>(visitedMethods), classDepthMap, targetDepth, callPath);
                    exceptions.addAll(calledMethodExceptions);
                } else {
                    logger.debug("Skipping method {} in class {} due to depth limitation (depth: {} > max: {})", calledMethod.getSimpleName(), methodClass.getSimpleName(), targetDepth, maxCrossClassDepth);
                }
            } else {
                logger.info("Cannot resolve method declaration for {}", executableRef);
            }

        } catch (Exception e) {
            logger.warn("Could not analyze exceptions for method call: {} - {}", invocation.toString(), e.getMessage());
        }

        return exceptions;
    }

    /**
     * Gets the depth for a class, handling both new classes and calls back to already-visited classes.
     * When calling back to an already-visited class, it prunes the depth map to maintain the correct call path.
     *
     * @param targetClass       the class to get/assign depth for
     * @param classDepthMap     map tracking assigned depths for each class
     * @param currentClassDepth the depth of the class making the call
     * @return the depth assigned to the target class
     */
    private int getOrAssignClassDepth(CtType<?> targetClass, Map<CtType<?>, Integer> classDepthMap, int currentClassDepth) {
        if (classDepthMap.containsKey(targetClass)) {
            int existingDepth = classDepthMap.get(targetClass);

            classDepthMap.entrySet().removeIf(entry -> entry.getValue() > existingDepth);

            return existingDepth;
        }

        int newDepth = currentClassDepth + 1;
        classDepthMap.put(targetClass, newDepth);


        return newDepth;
    }


    private ApiResponses resolveExceptionResponse(CtType<?> thrownType) {
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
        HttpStatus status = extractStatusCode(exceptionName, EXCEPTION_STATUS_MAP, "throw exception");
        return status != null ? createApiResponse(status) : null;
    }

    private ApiResponses createApiResponse(HttpStatus status) {
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

    abstract protected Class<?> getResponseStatusClass();

// endregion Exception detection

// region helpers

    boolean isListFactoryCall(CtInvocation<?> inv) {
        String methodName = inv.getExecutable().getSimpleName();
        String declaringType = inv.getExecutable().getDeclaringType().getQualifiedName();
        return (declaringType.equals("java.util.List") || declaringType.equals("java.util.Arrays")) && (methodName.equals("of") || methodName.equals("asList"));
    }

    // endregion helpers

}
