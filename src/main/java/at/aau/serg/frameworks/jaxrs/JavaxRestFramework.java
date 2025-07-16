package at.aau.serg.frameworks.jaxrs;

import at.aau.serg.frameworks.*;
import at.aau.serg.frameworks.jaxrs.adapters.mappings.JavaxRestOperationAnnotationAdapter;
import at.aau.serg.frameworks.jaxrs.adapters.parameters.JavaxHeaderParamAdapter;
import at.aau.serg.frameworks.jaxrs.adapters.parameters.JavaxPathParamAdapter;
import at.aau.serg.frameworks.jaxrs.adapters.parameters.JavaxQueryParamAdapter;
import at.aau.serg.frameworks.validation.ValidationAnnotationProviderFactory;
import at.aau.serg.interceptors.JakartaOperationResponseCodeInterceptor;
import at.aau.serg.parsers.HttpMethod;
import com.github.jrcodeza.schema.generator.DataTypeTransformer;
import com.github.jrcodeza.schema.generator.MethodResponseExtractor;
import com.github.jrcodeza.schema.generator.interceptors.OperationInterceptor;
import com.github.jrcodeza.schema.generator.util.SchemaGeneratorHelper;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import static at.aau.serg.frameworks.utils.AnnotationUtils.getAnnotation;

public class JavaxRestFramework extends AbstractJaxRsFramework {
    ValidationAnnotationProvider validationAnnotationProvider = new ValidationAnnotationProviderFactory().getCompositeProvider();

    @Override
    public String getIdentifier() {
        return "JavaX";
    }

    // TODO add Javax Interceptor
    @Override
    public OperationInterceptor getOperationResponseCodeInterceptor(List<CtType<?>> globalExceptionHandlerClasses,
                                                                    DataTypeTransformer dataTypeTransformer,
                                                                    SchemaGeneratorHelper schemaHelper,
                                                                    MethodResponseExtractor methodResponseExtractor) {
        return new JakartaOperationResponseCodeInterceptor(globalExceptionHandlerClasses, dataTypeTransformer, schemaHelper, methodResponseExtractor);
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
    public Set<String> getKeyAnnotations() {
        return Set.of("javax.ws.rs.Path");
    }

    @Override
    public boolean isGlobalExceptionHandler(String annotationName, CtType<?> type) {
        if (getControllerAdviceAnnotations().contains(annotationName)) {
            for (CtTypeReference<?> interfaceRef : type.getSuperInterfaces()) {
                String qualifiedName = interfaceRef.getQualifiedName();
                if (qualifiedName.equals("javax.ws.rs.ext.ExceptionMapper")) {
                    return true;
                }
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
    public Class<?> getAsyncResultWrapper() {
        return CompletionStage.class;
    }

    @Override
    public Class<? extends Annotation> getParameterGroupAnnotation() {
        return BeanParam.class;
    }


    @Override
    public CtParameter<?> findRequestBody(List<CtParameter<?>> parameters) {
        return parameters.stream()
                .filter(param -> !hasAnyAnnotationDisqualifyingParameterAsRequestBody(param))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Optional<Annotation> findPostMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, POST.class);
    }

    @Override
    public Optional<Annotation> findPutMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, PUT.class);
    }

    @Override
    public Optional<Annotation> findPatchMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, PATCH.class);
    }

    @Override
    public Optional<Annotation> findGetMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, GET.class);
    }

    @Override
    public Optional<Annotation> findDeleteMappingAnnotation(CtMethod<?> method) {
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

    private boolean isParameterTypeString(CtParameter<?> parameter) {
        String parameterType = parameter.getType().getQualifiedName();
        return parameterType.equals("java.lang.String");
    }

    /**
     * Returns true if parameter should be disqualified as a request body.
     * Disqualifies if parameter has JAX-RS binding annotations (@PathParam, @QueryParam, @Context, etc.)
     * or if parameter type is a framework-injected type (SecurityContext, HttpHeaders, UriInfo)
     * In Quarkus, framework-injected types can have @Context but can also be used without this annotation
     */
    private boolean hasAnyAnnotationDisqualifyingParameterAsRequestBody(CtParameter<?> parameter) {
        return isJavaxMethodAnnotationParameter(parameter) || isJavaxConfigurationAnnotation(parameter);
    }

    private boolean isJavaxConfigurationAnnotation(CtParameter<?> parameter) {
        String typeName = parameter.getType().getQualifiedName();
        return typeName.equals("javax.ws.rs.core.SecurityContext") ||
                typeName.equals("javax.ws.rs.core.UriInfo") ||
                typeName.equals("javax.ws.rs.core.HttpHeaders");
    }

    private boolean isJavaxMethodAnnotationParameter(CtParameter<?> parameter) {
        return parameter.getAnnotations().stream()
                .map(CtAnnotation::getAnnotationType)
                .map(CtTypeReference::getQualifiedName)
                .anyMatch(this::isJavaxParameterAnnotation);
    }

    // Javax was used previously before it was replaced by Jakarta
    private boolean isJavaxParameterAnnotation(String annotationName) {
        return annotationName.equals("javax.ws.rs.PathParam") ||
                annotationName.equals("javax.ws.rs.QueryParam") ||
                annotationName.equals("javax.ws.rs.HeaderParam") ||
                annotationName.equals("javax.ws.rs.CookieParam") ||
                annotationName.equals("javax.ws.rs.MatrixParam") ||
                annotationName.equals("javax.ws.rs.BeanParam") ||
                annotationName.equals("javax.ws.rs.core.Context");
    }
}