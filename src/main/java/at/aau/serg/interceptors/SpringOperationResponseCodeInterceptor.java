package at.aau.serg.interceptors;

import com.github.jrcodeza.schema.generator.interceptors.OperationInterceptor;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SpringOperationResponseCodeInterceptor implements OperationInterceptor {
    List<CtType<?>> globalExceptionHandlerClasses;

    public SpringOperationResponseCodeInterceptor(List<CtType<?>> globalExceptionHandlerClasses) {
        this.globalExceptionHandlerClasses = globalExceptionHandlerClasses;
    }

    @Override
    public void intercept(Method method, Operation transformedOperation) {
        throw new NotImplementedException();
    }

    @Override
    public void intercept(CtMethod<?> method, Operation transformedOperation) {
        // traverse the method's source code to identify additional statements containing response codes
        ArrayList<CtThrow> throwsStatements = new ArrayList<>();
        List<HttpStatus> statusCodesFromResponseEntities = new ArrayList<>();
        AtomicInteger numStatements = new AtomicInteger(0);

        recordRelevantStatements(method.getBody(), throwsStatements, statusCodesFromResponseEntities, numStatements);

        List<HttpStatus> statusCodesFromExceptions = transformThrowsStatementsToStatusCodes(method, throwsStatements);
        statusCodesFromResponseEntities = statusCodesFromResponseEntities.stream().distinct().sorted().collect(Collectors.toList());

        // apply all status codes to the OAS responses
        ApiResponses existingMethodResponses = transformedOperation.getResponses();
        // might reuse existing previously analyzed response, strongly assuming there is one
        ApiResponse existingResponse = existingMethodResponses.values().stream().findAny().get();

        boolean containsOnlyExceptions = numStatements.get() == throwsStatements.size() && statusCodesFromExceptions.size() > 0;
        boolean usesExplicitResponseEntities = statusCodesFromResponseEntities.size() > 0;
        assert !containsOnlyExceptions || !usesExplicitResponseEntities; // both cannot be possible

        if (containsOnlyExceptions || usesExplicitResponseEntities) {
            // remove the existing 200 response because there are either
            //  a) only error responses or
            //  b) only explicit ResponseEntity declarations which use their own 200
            existingMethodResponses = new ApiResponses();
        }

        for (HttpStatus sc : statusCodesFromResponseEntities) {
            ApiResponse apiResponse;
            if (sc.equals(HttpStatus.OK)) {
                // reuse the existing response already containing body etc
                apiResponse = existingResponse;
            } else {
                apiResponse = new ApiResponse();
                apiResponse.setDescription(sc.getReasonPhrase());
                // todo consider response body?
            }
            existingMethodResponses.addApiResponse(String.valueOf(sc.value()), apiResponse);
        }

        for (HttpStatus sc : statusCodesFromExceptions) {
            var apiResponse = new ApiResponse();
            apiResponse.setDescription(sc.getReasonPhrase());

            existingMethodResponses.addApiResponse(String.valueOf(sc.value()), apiResponse);
        }

        transformedOperation.setResponses(existingMethodResponses);
    }

    // region throws statement to http status code transformation
    //  todo extract

    /**
     * Handling single Java method with contained throws statements,
     * converts to HttpStatus codes.
     */
    public List<HttpStatus> transformThrowsStatementsToStatusCodes
    (CtMethod<?> method, ArrayList<CtThrow> throwsStatements) {
        List<HttpStatus> statusCodes = new ArrayList<>();

        // add the http response code for each throws statement
        for (CtThrow throwsStatement : throwsStatements) {

            // local controller's exception handlers
            HttpStatus statusCode = tryExtractHttpStatusFromLocalExceptionHandler(method, throwsStatement);

            if (statusCode == null) {
                // global exception advice
                statusCode = tryExtractHttpStatusFromExceptionHandlerAdvice(throwsStatement);
            }

            if (statusCode == null) {
                statusCode = tryExtractHttpStatusFromExceptionConstructor(throwsStatement);
            }

            if (statusCode == null) {
                // fallback to 500
                statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
            }

            statusCodes.add(statusCode);
        }

        return statusCodes.stream().distinct().sorted().collect(Collectors.toList());
    }

    private HttpStatus tryExtractHttpStatusFromLocalExceptionHandler(CtMethod<?> method, CtThrow throwsStatement) {
        CtType<?> exceptionType = throwsStatement.getThrownExpression().getType().getTypeDeclaration();

        // iterate class methods for a local exceptions handler annotated method
        for (var potentialHandlerMethod : method.getDeclaringType().getMethods()) {
            ExceptionHandlerResponseStatusMapping handlerMapping = getExceptionHandlerResponseStatusMapping(potentialHandlerMethod, exceptionType);
            if (handlerMapping != null) {
                return handlerMapping.returnStatus;
            }
        }

        return null;
    }

    /**
     * A simple performance-improving cache for inside this interceptor.
     * TODO should be moved to dedicated analysis
     */
    private Map<CtType<?>, HttpStatus> exceptionReturnStatusMappings = new HashMap();

    // TODO extract from here into a single setup phase storing the exceptionType -> returnStatus mappings
    private HttpStatus tryExtractHttpStatusFromExceptionHandlerAdvice(CtThrow throwsStatement) {
        CtType<?> exceptionType = throwsStatement.getThrownExpression().getType().getTypeDeclaration();

        if (exceptionReturnStatusMappings.containsKey(exceptionType)) {
            return exceptionReturnStatusMappings.get(exceptionType);
        }

        for (CtType<?> controllerAdviceClass : globalExceptionHandlerClasses) {

            for (var adviceMethod : controllerAdviceClass.getMethods()) {

                ExceptionHandlerResponseStatusMapping handlerMapping = getExceptionHandlerResponseStatusMapping(adviceMethod, exceptionType);

                if (handlerMapping != null) {
                    // return the advice for the annotated method, if matched
                    exceptionReturnStatusMappings.put(handlerMapping.exceptionType, handlerMapping.returnStatus);
                    return handlerMapping.returnStatus;
                }
            }
        }

        // no advice found for the exception
        exceptionReturnStatusMappings.put(exceptionType, null);
        return null;
    }

    private static final String EXCEPTION_HANDLER_ANNOTATION = "org.springframework.web.bind.annotation.ExceptionHandler";
    private static final String RESPONSE_STATUS_ANNOTATION = "org.springframework.web.bind.annotation.ResponseStatus";

    private ExceptionHandlerResponseStatusMapping getExceptionHandlerResponseStatusMapping
            (CtMethod<?> adviceMethod, CtType<?> exceptionType) {
        boolean isExceptionHandlerAnnotationMatch = false;
        HttpStatus returnStatus = null;

        for (var annotation : adviceMethod.getAnnotations()) {
            // cannot use annotation.getActualAnnotation() because of custom exceptions in the project

            if (EXCEPTION_HANDLER_ANNOTATION
                    .equals(annotation.getAnnotationType().toString())) {
                // check if ExceptionHandler contains the exceptionType
                CtExpression<?> arg = annotation.getValue("value");
                if (arg instanceof CtNewArray) {
                    for (CtExpression<?> param : ((CtNewArray<?>) arg).getElements()) {
                        if (annotationParameterMatchesExceptionClass(param, exceptionType)) {
                            isExceptionHandlerAnnotationMatch = true;
                        }
                    }
                } else if (annotationParameterMatchesExceptionClass(arg, exceptionType)) {
                    isExceptionHandlerAnnotationMatch = true;
                }

            } else if (RESPONSE_STATUS_ANNOTATION
                    .equals(annotation.getAnnotationType().toString())) {
                var arg = annotation.getValue("value");
                if (arg instanceof CtFieldRead) {
                    returnStatus = getHttpStatusCodeFromCtFieldRead((CtFieldRead<?>) arg);
                }
            }
        }

        if (isExceptionHandlerAnnotationMatch) {
            return new ExceptionHandlerResponseStatusMapping(exceptionType, returnStatus);
        } else {
            return null;
        }
    }

    /**
     * Checks if the (ExceptionHandler annotation's) parameter matches the exception class.
     *
     * @param param
     * @param exceptionType
     * @return
     */
    private boolean annotationParameterMatchesExceptionClass(CtExpression<?> param, CtType<?> exceptionType) {
        // compare the currently analyzed exception's class name with the annotation info
        return param instanceof CtFieldRead && ((CtFieldRead<?>) param).getVariable().getQualifiedName() // getSimpleName only returns .class
                .contains(exceptionType.getSimpleName());
    }

    /**
     * Trys to extract the HTTP status code from the throws statement's exception constructor call
     * e.g., throw new CustomRestException(HttpStatus.BAD_REQUEST)
     *
     * @param throwsStatement
     * @return
     */
    private HttpStatus tryExtractHttpStatusFromExceptionConstructor(CtThrow throwsStatement) {
        if (throwsStatement.getThrownExpression() instanceof CtConstructorCall) {
            CtConstructorCall<?> exceptionConstructorCall = (CtConstructorCall<?>) throwsStatement.getThrownExpression();

            for (CtExpression<?> arg : exceptionConstructorCall.getArguments()) {
                if (arg.getType().isSubtypeOf(new TypeFactory().get(HttpStatus.class).getReference())) {
                    // HTTP status code is part of the constructor call
                    if (arg instanceof CtFieldRead) {
                        HttpStatus returnStatusCode = getHttpStatusCodeFromCtFieldRead((CtFieldRead<?>) arg);
                        return returnStatusCode;
                    }
                }
            }
        }

        return null;
    }

    // endregion throws statement analysis

    /**
     * Get the Status Code from a class reference in a method call,
     * e.g., sampleCall(HttpStatus.OK)
     *
     * @param arg
     * @return might be null
     */
    private HttpStatus getHttpStatusCodeFromCtFieldRead(CtFieldRead<?> arg) {
        if (!isTypeEquivalent(((CtTypeAccess<?>) arg.getTarget()).getAccessedType(), HttpStatus.class))
            return null;

        String returnStatusCodeName = arg.getVariable().getSimpleName();

        HttpStatus returnStatusCode = Enum.valueOf(HttpStatus.class, returnStatusCodeName);
        return returnStatusCode;
    }

    /**
     * Spoon returns blocks, ifs, loops as single statement. Unwrap and traverse them recursively.
     *
     * @param statement
     */
    private void recordRelevantStatements(CtStatement statement,
                                          @NotNull List<CtThrow> ctThrows,
                                          @NotNull List<HttpStatus> responseEntityDefinedStatusCodes,
                                          AtomicInteger numStatements) {
        // todo throws in signature
        if (statement instanceof CtBlock) {
            for (CtStatement innerStatement : ((CtBlock<?>) statement).getStatements()) {
                recordRelevantStatements(innerStatement, ctThrows, responseEntityDefinedStatusCodes, numStatements);
            }

        } else if (statement instanceof CtSynchronized) {
            recordRelevantStatements(((CtSynchronized) statement).getBlock(), ctThrows, responseEntityDefinedStatusCodes, numStatements);

        } else if (statement instanceof CtIf) {
            recordRelevantStatements(((CtIf) statement).getThenStatement(), ctThrows, responseEntityDefinedStatusCodes, numStatements);
            recordRelevantStatements(((CtIf) statement).getElseStatement(), ctThrows, responseEntityDefinedStatusCodes, numStatements);

        } else if (statement instanceof CtLoop) {
            recordRelevantStatements(((CtLoop) statement).getBody(), ctThrows, responseEntityDefinedStatusCodes, numStatements);

        } else if (statement instanceof CtSwitch) {
            for (CtCase<?> caseStatement : ((CtSwitch<?>) statement).getCases()) {
                for (var innerStatement : caseStatement.getStatements()) {
                    recordRelevantStatements(innerStatement, ctThrows, responseEntityDefinedStatusCodes, numStatements);
                }
            }

            // todo consider try and catch blocks and try with resource for unhandled exceptions
            // todo consider assertion statements

        } else if (statement instanceof CtThrow) {
            // record the throw
            ctThrows.add((CtThrow) statement);
            numStatements.incrementAndGet();

        } else if (statement != null) { // normal statement, I guess
            numStatements.incrementAndGet();

            var httpStatusCode = tryTransformResponseEntityStatement(statement);
            httpStatusCode.ifPresent(responseEntityDefinedStatusCodes::add);
        }
    }

    // region ResponseEntity to http status code transformation
    //  todo extract

    /**
     * Check for ResponseEntity use in the statement
     * e.g., ResponseEntity response = new ResponseEntity(HttpStatus.NOT_FOUND);
     * e.g., ResponseEntity response = ResponseEntity.ok(someBody);
     * e.g., return new ResponseEntity(someBody, HttpStatus.OK);
     * e.g., return ResponseEntity.ok(someBody);
     *
     * @param statement
     */
    private Optional<HttpStatus> tryTransformResponseEntityStatement(CtStatement statement) {
        Optional<HttpStatus> statusCode = Optional.empty();

        if (statement instanceof CtVariable
                && isTypeEquivalent(((CtVariable<?>) statement).getType(), ResponseEntity.class)) {
            CtVariable<?> variableDeclarationStatement = (CtVariable<?>) statement;

            if (variableDeclarationStatement.getDefaultExpression() instanceof CtConstructorCall) {
                // e.g., ResponseEntity response = new ResponseEntity(HttpStatus.NOT_FOUND);
                var constructorCallStatement = (CtConstructorCall<ResponseEntity>) variableDeclarationStatement.getDefaultExpression();
                statusCode = extractHttpStatusFromResponseEntityConstrCall(constructorCallStatement);

            } else if (variableDeclarationStatement.getDefaultExpression() instanceof CtInvocation) {
                // e.g., ResponseEntity response = ResponseEntity.ok(someBody);
                var methodInvocationStatement = (CtInvocation<?>) variableDeclarationStatement.getDefaultExpression();
                statusCode = extractHttpStatusFromResponseEntityMethodInvocation(methodInvocationStatement);
            }

        } else if (statement instanceof CtReturn
                && ((CtReturn<?>) statement).getReturnedExpression() != null // ignore 'return;' statements
                && isTypeEquivalent(((CtReturn<?>) statement).getReturnedExpression().getType(), ResponseEntity.class)) {
            CtReturn<?> returnStatement = (CtReturn<?>) statement;

            if (returnStatement.getReturnedExpression() instanceof CtConstructorCall) {
                // e.g., return new ResponseEntity(someBody, HttpStatus.OK);
                var constructorCallStatement = (CtConstructorCall<ResponseEntity>) returnStatement.getReturnedExpression();
                statusCode = extractHttpStatusFromResponseEntityConstrCall(constructorCallStatement);

            } else if (returnStatement.getReturnedExpression() instanceof CtInvocation) {
                // e.g., return ResponseEntity.ok(someBody);
                var methodInvocationStatement = (CtInvocation<?>) returnStatement.getReturnedExpression();
                statusCode = extractHttpStatusFromResponseEntityMethodInvocation(methodInvocationStatement);
            }
        }

        return statusCode;
    }

    /**
     * Extracts the status code of ResponseEntity method invocations
     * e.g., ResponseEntity.ok(bodyObj)
     * <p>
     * TODO extract from ResponseEntity builder methods
     *
     * @param ctInvocation
     * @return
     */
    private Optional<HttpStatus> extractHttpStatusFromResponseEntityMethodInvocation(CtInvocation<?> ctInvocation) {
        var methodName = ctInvocation.getExecutable().getSimpleName();
        if ("ok".equals(methodName)) {
            return Optional.of(HttpStatus.OK);
        }

        return Optional.empty();
    }

    /**
     * Extracts the status code of ResponseEntity constructor call
     * e.g., new ResponseEntity(HttpStatus.NOT_FOUND)
     * e.g., new ResponseEntity(bodyObj, HttpStatus.OK)
     *
     * @param constructorCallStatement
     * @return
     */
    private Optional<HttpStatus> extractHttpStatusFromResponseEntityConstrCall
    (CtConstructorCall<ResponseEntity> constructorCallStatement) {
        // iterate arguments for the HttpStatus
        Optional<HttpStatus> httpStatusCode = constructorCallStatement.getArguments().stream()
                .filter(arg -> arg instanceof CtFieldRead)
                .map(arg -> getHttpStatusCodeFromCtFieldRead((CtFieldRead<?>) arg))
                .filter(statusCode -> statusCode != null)
                .findAny(); // we assume there is only one status code in the constructor call

        return httpStatusCode;
    }

    // endregion

    private boolean isTypeEquivalent(CtTypeReference<?> type, Class<?> clazz) {
        return type != null && clazz != null
                && type.isSubtypeOf(new TypeFactory().get(clazz).getReference());
    }
}
