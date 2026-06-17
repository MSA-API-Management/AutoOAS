package at.aau.serg.frameworks.jaxrs;

import at.aau.serg.frameworks.*;
import at.aau.serg.frameworks.jaxrs.adapters.mappings.JavaxRestOperationAnnotationAdapter;
import at.aau.serg.frameworks.jaxrs.adapters.parameters.JavaxHeaderParamAdapter;
import at.aau.serg.frameworks.jaxrs.adapters.parameters.JavaxPathParamAdapter;
import at.aau.serg.frameworks.jaxrs.adapters.parameters.JavaxQueryParamAdapter;
import at.aau.serg.frameworks.validation.ValidationAnnotationProviderFactory;
import at.aau.serg.interceptors.JavaxOperationResponseCodeInterceptor;
import at.aau.serg.parsers.HttpMethod;
import at.aau.serg.util.SpoonUtils;
import com.github.jrcodeza.schema.generator.DataTypeTransformer;
import com.github.jrcodeza.schema.generator.MethodResponseExtractor;
import com.github.jrcodeza.schema.generator.interceptors.OperationInterceptor;
import com.github.jrcodeza.schema.generator.util.SchemaGeneratorHelper;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import javax.ws.rs.*;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static at.aau.serg.frameworks.utils.AnnotationUtils.getAnnotation;

public class JavaxRestFramework extends AbstractJaxRsFramework {
    public static final String JAVAX_EXCEPTION_MAPPER_CLASS = "javax.ws.rs.ext.ExceptionMapper";

    ValidationAnnotationProvider validationAnnotationProvider = new ValidationAnnotationProviderFactory().getCompositeProvider();

    @Override
    public String getIdentifier() {
        return super.getIdentifier() + " (Java EE)";
    }

    @Override
    public OperationInterceptor getOperationResponseCodeInterceptor(List<CtType<?>> globalExceptionHandlerClasses,
                                                                    DataTypeTransformer dataTypeTransformer,
                                                                    SchemaGeneratorHelper schemaHelper,
                                                                    MethodResponseExtractor methodResponseExtractor) {
        return new JavaxOperationResponseCodeInterceptor(globalExceptionHandlerClasses, dataTypeTransformer, schemaHelper, methodResponseExtractor, exceptionLoggingEnabled);
    }

    @Override
    public List<String> getControllerAnnotations() {
        return List.of("javax.ws.rs.Path");
    }

    /**
     * An exception contains @Provider as annotation and also
     * implements the javax.ws.rs.ext.ExceptionMapper
     */
    @Override
    public List<String> getControllerAdviceAnnotations() {
        return List.of("javax.ws.rs.ext.Provider");
    }

    @Override
    public String getApplicationPathAnnotation() {
        return "javax.ws.rs.ApplicationPath";
    }

    @Override
    public Set<String> getKeyAnnotations() {
        return Set.of("javax.ws.rs.Path");
    }

    @Override
    public boolean isGlobalExceptionHandler(String annotationName, CtType<?> type) {
        if (getControllerAdviceAnnotations().contains(annotationName)) {
            for (CtTypeReference<?> interfaceRef : type.getSuperInterfaces()) {
                String qualifiedName = interfaceRef.getQualifiedName();
                if (qualifiedName.equals(JAVAX_EXCEPTION_MAPPER_CLASS)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isApplicationPath(String annotationName, CtType<?> type) {
        if(getApplicationPathAnnotation().equals(annotationName)) {
            CtTypeReference<?> superclassRef = type.getSuperclass();

            if(superclassRef != null) {
                String qualifiedName = superclassRef.getQualifiedName();
                return qualifiedName.equals("javax.ws.rs.core.Application");
            }
        }
        return false;
    }
    @Override
    public Class<?> getResponseWrapper() {
        return Response.class;
    }

    @Override
    public Class<?> getSupportedFileType() {
        return MultivaluedMap.class;
    }

    @Override
    public Class<? extends Annotation> getParameterGroupAnnotation() {
        return BeanParam.class;
    }

    /**
     * TODO de-duplicate this and JakartaRestFramework::getSubResourcesInController
     *
     * @param controllerType
     * @return
     */
    @Override
    public List<SubResource> getSubResourcesInController(CtType<?> controllerType) {
        var resultList = new ArrayList<SubResource>();

        for (CtMethod<?> method : controllerType.getMethods()) {
            CtType<?> methodReturnType = method.getType().getTypeDeclaration();
            Optional<Path> pathAnnotation = getAnnotation(method, Path.class);

            if (pathAnnotation.isEmpty()
                    || !classContainsHandlerMethodOrSubResource(methodReturnType)  // then its most probably a data class
                    || SpoonUtils.areTypesEqual(controllerType, methodReturnType)) // then its a recursive path providing the same class again
                continue;

            // found a method with Path annotation providing a sub-resource
            String subResourcePath = pathAnnotation.get().value();
            resultList.add(new SubResource(methodReturnType, subResourcePath, method));
        }

        return resultList;
    }

    @Override
    public CtParameter<?> findRequestBody(List<CtParameter<?>> parameters) {
        return parameters.stream()
                .filter(param -> !hasAnyAnnotationDisqualifyingParameterAsRequestBody(param))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getFormFieldName(CtParameter<?> parameter) {
        return parameter.getAnnotation(javax.ws.rs.FormParam.class).value();
    }

    @Override
    public String getFormParamAnnotationName() {
        return "javax.ws.rs.FormParam";
    }

    @Override
    public Optional<? extends Annotation> findPostMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, POST.class);
    }

    @Override
    public Optional<? extends Annotation> findPutMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, PUT.class);
    }

    @Override
    public Optional<? extends Annotation> findPatchMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, PATCH.class);
    }

    @Override
    public Optional<? extends Annotation> findGetMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, GET.class);
    }

    @Override
    public Optional<? extends Annotation> findDeleteMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, DELETE.class);
    }


    @Override
    public Optional<RestOperationAnnotation> findClassRequestMappingAnnotation(CtType<?> clazz) {
        for (CtAnnotation<?> annotation : clazz.getAnnotations()) {
            String annotationTypeName = annotation.getAnnotationType().getQualifiedName();
            if (annotationTypeName.equals(Path.class.getName())) {
                return Optional.of(new JavaxRestOperationAnnotationAdapter(clazz));
            }
        }
        return Optional.empty();
    }

    @Override
    public RestOperationAnnotation convertToRequestAnnotation(Annotation annotation, CtMethod<?> method) {
        if (annotation instanceof POST) {
            return new JavaxRestOperationAnnotationAdapter(method, HttpMethod.POST);
        }
        if (annotation instanceof PUT) {
            return new JavaxRestOperationAnnotationAdapter(method, HttpMethod.PUT);
        }
        if (annotation instanceof PATCH) {
            return new JavaxRestOperationAnnotationAdapter(method, HttpMethod.PATCH);
        }
        if (annotation instanceof GET) {
            return new JavaxRestOperationAnnotationAdapter(method, HttpMethod.GET);
        }
        if (annotation instanceof DELETE) {
            return new JavaxRestOperationAnnotationAdapter(method, HttpMethod.DELETE);
        }

        throw new IllegalArgumentException("No supported annotation found");
    }

    @Override
    public PathVariableAnnotation tryConvertPathVariableAnnotation(CtParameter<?> parameter) {
        PathParam annotation = parameter.getAnnotation(PathParam.class);
        if (annotation != null) {
            return new JavaxPathParamAdapter(annotation);
        }

        return null;
    }

    @Override
    public RequestParamAnnotation tryConvertRequestParamAnnotation(CtParameter<?> parameter) {
        // Checks if NotEmpty or NotNull annotation exists and if parameter is type String.
        // int/long/double are required in OpenAPI for NotNull but not in an actual request as they have a default value
        boolean isParameterRequired = hasValidationAnnotation(parameter) && isParameterTypeString(parameter);

        QueryParam annotation = parameter.getAnnotation(QueryParam.class);
        if (annotation != null) {
            JavaxQueryParamAdapter adapter = new JavaxQueryParamAdapter(annotation);
            adapter.setRequired(isParameterRequired);
            return adapter;
        }

        return null;
    }

    @Override
    public RequestHeaderAnnotation tryConvertRequestHeaderAnnotation(CtParameter<?> parameter) {
        boolean isParameterRequired = hasValidationAnnotation(parameter) && isParameterTypeString(parameter);

        HeaderParam annotation = parameter.getAnnotation(HeaderParam.class);
        if (annotation != null) {
            JavaxHeaderParamAdapter adapter = new JavaxHeaderParamAdapter(annotation);
            adapter.setRequired(isParameterRequired);
            return adapter;
        }

        return null;
    }

    private boolean hasValidationAnnotation(CtParameter<?> parameter) {
        List<CtAnnotation<? extends Annotation>> annotations = parameter.getAnnotations();

        return annotations.stream()
                .anyMatch(annotation -> {
                            Annotation actualAnnotation = annotation.getActualAnnotation();
                            return validationAnnotationProvider.isNotNullAnnotation(actualAnnotation) ||
                                    validationAnnotationProvider.isNotEmptyAnnotation(actualAnnotation);
                        }
                );
    }

    /**
     * Returns true if parameter should be disqualified as a request body.
     * Disqualifies if parameter has JAX-RS binding annotations (@PathParam, @QueryParam, @Context, etc.)
     * or if parameter type is a framework-injected type (SecurityContext, HttpHeaders, UriInfo)
     * In Quarkus, framework-injected types can have @Context but can also be used without this annotation
     */
    private boolean hasAnyAnnotationDisqualifyingParameterAsRequestBody(CtParameter<?> parameter) {
        return isRestMethodAnnotatedParameter(parameter) || isJavaxConfigurationAnnotation(parameter);
    }

    private boolean isJavaxConfigurationAnnotation(CtParameter<?> parameter) {
        String typeName = parameter.getType().getQualifiedName();
        return typeName.equals("javax.ws.rs.core.SecurityContext") ||
                typeName.equals("javax.ws.rs.core.UriInfo") ||
                typeName.equals("javax.ws.rs.core.HttpHeaders");
    }

    @Override
    protected boolean isRestParameterAnnotation(String annotationName) {
        return annotationName.equals("javax.ws.rs.PathParam") ||
                annotationName.equals("javax.ws.rs.FormParam") ||
                annotationName.equals("javax.ws.rs.QueryParam") ||
                annotationName.equals("javax.ws.rs.HeaderParam") ||
                annotationName.equals("javax.ws.rs.CookieParam") ||
                annotationName.equals("javax.ws.rs.MatrixParam") ||
                annotationName.equals("javax.ws.rs.BeanParam") ||
                annotationName.equals("javax.ws.rs.container.Suspended") ||
                annotationName.equals("javax.ws.rs.core.Context");
    }

    @Override
    protected boolean isRestHandlerMethodOrSubResourceAnnotation(String annotationName) {
        return annotationName.equals("javax.ws.rs.POST") ||
                annotationName.equals("javax.ws.rs.PUT") ||
                annotationName.equals("javax.ws.rs.PATCH") ||
                annotationName.equals("javax.ws.rs.GET") ||
                annotationName.equals("javax.ws.rs.DELETE") ||
                annotationName.equals("javax.ws.rs.HEAD") ||
                annotationName.equals("javax.ws.rs.OPTIONS") ||
                annotationName.equals("javax.ws.rs.Path");
    }
}