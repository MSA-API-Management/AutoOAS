package at.aau.serg.interceptors;

import com.github.jrcodeza.schema.generator.DataTypeTransformer;
import com.github.jrcodeza.schema.generator.interceptors.OperationInterceptor;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.NotImplementedException;
import org.javatuples.Pair;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

public class JakartaOperationResponseCodeInterceptor implements OperationInterceptor {
    List<CtType<?>> adviceClasses; // todo check for equivalent of controllerAdviceClasses
    DataTypeTransformer dataTypeTransformer;

    public JakartaOperationResponseCodeInterceptor(List<CtType<?>> adviceClasses, DataTypeTransformer dataTypeTransformer) {
        this.adviceClasses = adviceClasses;
        this.dataTypeTransformer = dataTypeTransformer;
    }

    @Override
    public void intercept(Method method, Operation transformedOperation) {
        throw new NotImplementedException();
    }

    @Override
    public void intercept(CtMethod<?> method, Operation transformedOperation) {
        ApiResponses existingMethodResponses = transformedOperation.getResponses();


        System.out.println(method);

        var responses = analyzeRestMethod(method);


        transformedOperation.setResponses(responses);
    }


    private ApiResponses analyzeRestMethod(CtMethod<?> method) {
        ApiResponses apiResponses = new ApiResponses();

        // limitation: only handle direct invocation at return statement
        for (var returnStatement : method.getElements(new TypeFilter<>(CtReturn.class))) {
            CtExpression<?> returned = returnStatement.getReturnedExpression();
            if (returned instanceof CtInvocation<?> inv) {
                var response = analyzeResponseInvocation(inv);
                if (response != null)
                    apiResponses.addApiResponse(response.getValue0(), response.getValue1());
            }
        }

        return apiResponses;
    }

    private Pair<String, ApiResponse> analyzeResponseInvocation(CtInvocation<?> inv) {
        CtExecutableReference<?> executable = inv.getExecutable();
        String methodName = executable.getSimpleName();

        if (methodName.equals("build")) {
            // detect builder call
            CtInvocation<?> baseInvocation = (CtInvocation<?>) inv.getTarget();
            return traceResponseCreationBackFromBuildCall(baseInvocation);
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
     * @return the response code and response type pair
     */
    private Pair<String, ApiResponse> traceResponseCreationBackFromBuildCall(CtInvocation<?> buildCallTarget) {
        ApiResponse response = new ApiResponse();
        String statusCode = null;

        CtExpression<?> curMethodInChain = buildCallTarget;
        while (curMethodInChain instanceof CtInvocation<?> method) {
            System.out.println(method);
            String methodName = method.getExecutable().getSimpleName();

            // handle response type
            if (methodName.equals("ok") || methodName.equals("entity")) {
                List<CtExpression<?>> args = method.getArguments();
                if (!args.isEmpty()) {
                    CtExpression<?> arg = args.getFirst();
                    response = extractPayloadTypeInfo(arg);
                }
            }

            // handle response code
            int responseStatus;
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
            } else if (methodName.equals("statusCode")) {
                // custom statusCode
                var statusCodeMethodArg = method.getArguments().getFirst();
                if (statusCodeMethodArg.getType() instanceof Response.StatusType statusTypeMethodArg) {
                    responseStatus = statusTypeMethodArg.getStatusCode();
                }
                else if(statusCodeMethodArg.getType() instanceof Integer) {

                }
            }
//
//            System.out.println("Status: " + statusCode);
//            System.out.println(response);


            curMethodInChain = method.getTarget();
        }


        return new Pair<String, ApiResponse>(statusCode, response);
    }

    private ApiResponse extractPayloadTypeInfo(CtExpression<?> expr) {
        ApiResponse response = null;

        // Case 1: Variable read (e.g., 'al')
        if (expr instanceof CtVariableRead<?> varRead) {
            CtVariable<?> varDecl = varRead.getVariable().getDeclaration();
            if (varDecl != null) {
                CtTypeReference<?> type = varDecl.getType();

                response = dataTypeTransformer.detectAndCreateApiResponseContent(type);

//                printType(type);
//                System.out.println(response);
            }
        }

        // Case 2: Factory call (e.g., List.of(...))
        else if (expr instanceof CtInvocation<?> call && isListFactoryCall(call)) {

            response = dataTypeTransformer.detectAndCreateApiResponseContent(call.getType());
//            List<CtExpression<?>> args = call.getArguments();
//            if (!args.isEmpty()) {
//                CtTypeReference<?> itemType = args.get(0).getType();
//
//                System.out.println("type = array");
//                System.out.println("contained type = " + itemType.getQualifiedName());
//            }
        }

        // Fallback: direct object
        else {
            response = dataTypeTransformer.detectAndCreateApiResponseContent(expr.getType());
//            CtTypeReference<?> type = expr.getType();
//            System.out.println("type = object");
//            System.out.println("object type = " + type.getQualifiedName());
        }

        return response;
    }

    private void printType(CtTypeReference<?> type) {
        if (isCollection(type)) {
            CtTypeReference<?> elementType = getFirstTypeArgument(type);
            if (elementType != null) {
                System.out.println("type = array");
                System.out.println("contained type = " + elementType.getQualifiedName());
//                        response.set$ref();
                // todo alex here, from operationstransofrmer
            }
        } else {

            System.out.println("type = object");
            System.out.println("object type = " + type.getQualifiedName());
        }
    }

    // region helpers
    boolean isCollection(CtTypeReference<?> type) {
        return type != null && type.isSubtypeOf(type.getFactory().Type().createReference(Collection.class));
    }

    CtTypeReference<?> getFirstTypeArgument(CtTypeReference<?> type) {
        if (type != null && !type.getActualTypeArguments().isEmpty()) {
            return type.getActualTypeArguments().get(0);
        }
        return null;
    }

    boolean isListFactoryCall(CtInvocation<?> inv) {
        String methodName = inv.getExecutable().getSimpleName();
        String declaringType = inv.getExecutable().getDeclaringType().getQualifiedName();
        return (declaringType.equals("java.util.List") || declaringType.equals("java.util.Arrays")) && (methodName.equals("of") || methodName.equals("asList"));
    }
// endregion helpers

}
