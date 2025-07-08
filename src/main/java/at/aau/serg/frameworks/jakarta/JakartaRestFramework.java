package at.aau.serg.frameworks.jakarta;

import at.aau.serg.frameworks.*;
import at.aau.serg.frameworks.jakarta.adapters.mappings.JakartaRestOperationAnnotationAdapter;
import at.aau.serg.frameworks.jakarta.adapters.parameters.JakartaHeaderParamAdapter;
import at.aau.serg.frameworks.jakarta.adapters.parameters.JakartaPathParamAdapter;
import at.aau.serg.frameworks.jakarta.adapters.parameters.JakartaQueryParamAdapter;
import at.aau.serg.frameworks.validation.ValidationAnnotationProviderFactory;
import at.aau.serg.interceptors.JakartaOperationResponseCodeInterceptor;
import at.aau.serg.parsers.HttpMethod;
import com.github.jrcodeza.schema.generator.DataTypeTransformer;
import com.github.jrcodeza.schema.generator.MethodResponseExtractor;
import com.github.jrcodeza.schema.generator.interceptors.OperationInterceptor;
import com.github.jrcodeza.schema.generator.util.SchemaGeneratorHelper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

import static at.aau.serg.frameworks.utils.AnnotationUtils.getAnnotation;

public class JakartaRestFramework implements RestFramework {
    ValidationAnnotationProvider validationAnnotationProvider = new ValidationAnnotationProviderFactory().getCompositeProvider();

    @Override
    public String getIdentifier() {
        return "Quarkus";
    }

    @Override
    public OperationInterceptor getOperationResponseCodeInterceptor(List<CtType<?>> globalExceptionHandlerClasses,
                                                                    DataTypeTransformer dataTypeTransformer,
                                                                    SchemaGeneratorHelper schemaHelper,
                                                                    MethodResponseExtractor methodResponseExtractor) {
        return new JakartaOperationResponseCodeInterceptor(globalExceptionHandlerClasses, dataTypeTransformer, schemaHelper, methodResponseExtractor);
    }

    @Override
    public Map<String, List<CtType<?>>> splitClassesOnProfiles(List<CtType<?>> controllerClasses) {
        // Jakarta does not define any profile functionality
        //  Jersey similarly does not provide profile functionality
        //  Quarkus does, @IfBuildProfile(allOf / anyOf = {"dev","prod"})
        //  Todo: We do not support Quarkus profiles currently
        return Map.of("default", controllerClasses);
    }

    @Override
    public String getProfileAnnotation() {
        return "";
    }

    @Override
    public List<String> getControllerAnnotations() {
        return List.of("jakarta.ws.rs.Path", "javax.ws.rs.Path");
    }

    /**
     * An exception contains @Provider as annotation and also
     * implements the jakarta.ws.rs.ext.ExceptionMapper
     */
    @Override
    public List<String> getControllerAdviceAnnotations() {
        return List.of("jakarta.ws.rs.ext.Provider", "javax.ws.rs.ext.Provider");
    }

    @Override
    public Set<String> getKeyAnnotations() {
        return Set.of(
                "jakarta.ws.rs.Path",
                "javax.ws.rs.Path"
        );
    }

    @Override
    public boolean isGlobalExceptionHandler(String annotationName, CtType<?> type) {
        if (getControllerAdviceAnnotations().contains(annotationName)) {
            for (CtTypeReference<?> interfaceRef : type.getSuperInterfaces()) {
                String qualifiedName = interfaceRef.getQualifiedName();
                if (qualifiedName.equals("jakarta.ws.rs.ext.ExceptionMapper")
                        || qualifiedName.equals("javax.ws.rs.ext.ExceptionMapper")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<String> getModelSchemaAnnotations() {
        return List.of();
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
    public Class<? extends Annotation> getRequestBodyAnnotation() {
        return null; // No specific request body annotation exists in quarkus
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
    public Optional<Annotation> findRequestMappingAnnotation(CtMethod<?> method) {
        return Optional.empty();    // no request mapping exists
    }

    @Override
    public Optional<RestOperationAnnotation> findClassRequestMappingAnnotation(CtType<?> clazz) {
        for (CtAnnotation<?> annotation : clazz.getAnnotations()) {
            String annotationTypeName = annotation.getAnnotationType().getQualifiedName();
            if (annotationTypeName.equals(Path.class.getName())) {
                return Optional.of(new JakartaRestOperationAnnotationAdapter(clazz));
            }
        }
        return Optional.empty();
    }

    @Override
    public RestOperationAnnotation convertToRequestAnnotation(Annotation annotation, CtMethod<?> method) {
        if (annotation instanceof POST) {
            return new JakartaRestOperationAnnotationAdapter(method, HttpMethod.POST);
        }
        if (annotation instanceof PUT) {
            return new JakartaRestOperationAnnotationAdapter(method, HttpMethod.PUT);
        }
        if (annotation instanceof PATCH) {
            return new JakartaRestOperationAnnotationAdapter(method, HttpMethod.PATCH);
        }
        if (annotation instanceof GET) {
            return new JakartaRestOperationAnnotationAdapter(method, HttpMethod.GET);
        }
        if (annotation instanceof DELETE) {
            return new JakartaRestOperationAnnotationAdapter(method, HttpMethod.DELETE);
        }
        if (annotation instanceof RequestMapping) {
            return null;
        }

        throw new IllegalArgumentException("No supported annotation found");
    }

    @Override
    public PathVariableAnnotation tryConvertPathVariableAnnotation(CtParameter<?> parameter) {
        PathParam annotation = parameter.getAnnotation(PathParam.class);
        if (annotation != null) {
            return new JakartaPathParamAdapter(annotation);
        }
        return null;
    }

    @Override
    public RequestParamAnnotation tryConvertRequestParamAnnotation(CtParameter<?> parameter) {
        QueryParam annotation = parameter.getAnnotation(QueryParam.class);

        if (annotation != null) {
            JakartaQueryParamAdapter adapter = new JakartaQueryParamAdapter(annotation);

            // Checks if NotEmpty or NotNull annotation exists and if parameter is type String.
            // int/long/double are required in OpenAPI for NotNull but not in an actual request as they have a default value
            if (hasValidationAnnotation(parameter) && isParameterTypeString(parameter)) {
                adapter.setRequired(true);
            }
            return adapter;
        }
        return null;
    }

    @Override
    public RequestHeaderAnnotation tryConvertRequestHeaderAnnotation(CtParameter<?> parameter) {
        HeaderParam annotation = parameter.getAnnotation(HeaderParam.class);

        if (annotation != null) {
            JakartaHeaderParamAdapter adapter = new JakartaHeaderParamAdapter(annotation);

            if (hasValidationAnnotation(parameter) && isParameterTypeString(parameter)) {
                adapter.setRequired(true);
            }
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

    @Override
    public HttpMethod[] getAllSupportedHttpMethods() {
        return HttpMethod.values();
    }

    @Override
    public boolean isAnyHttpMethodWithRequestBody(HttpMethod... methods) {
        return Stream.of(methods).anyMatch(method -> EnumSet.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH).contains(method));
    }

    @Override
    public HttpStatus getVoidMethodStatusCode() {
        return HttpStatus.NO_CONTENT;
    }

    /**
     * Returns true if parameter should be disqualified as a request body.
     * Disqualifies if parameter has JAX-RS binding annotations (@PathParam, @QueryParam, @Context, etc.)
     * or if parameter type is a framework-injected type (SecurityContext, HttpHeaders, UriInfo)
     * In Quarkus, framework-injected types can have @Context but can also be used without this annotation
     */
    private boolean hasAnyAnnotationDisqualifyingParameterAsRequestBody(CtParameter<?> parameter) {
        return isJakartaMethodAnnotationParameter(parameter) || isJakartaConfigurationAnnotation(parameter);
    }

    private boolean isJakartaConfigurationAnnotation(CtParameter<?> parameter) {
        String typeName = parameter.getType().getQualifiedName();
        return typeName.equals("javax.ws.rs.core.SecurityContext") ||
                typeName.equals("javax.ws.rs.core.UriInfo") ||
                typeName.equals("javax.ws.rs.core.HttpHeaders") ||
                typeName.equals("jakarta.ws.rs.core.SecurityContext") ||
                typeName.equals("jakarta.ws.rs.core.UriInfo") ||
                typeName.equals("jakarta.ws.rs.core.HttpHeaders");
    }

    private boolean isJakartaMethodAnnotationParameter(CtParameter<?> parameter) {
        return parameter.getAnnotations().stream()
                .map(CtAnnotation::getAnnotationType)
                .map(CtTypeReference::getQualifiedName)
                .anyMatch(this::isJakartaParameterAnnotation);
    }

    // Javax was used previously before it was replaced by Jakarta
    private boolean isJakartaParameterAnnotation(String annotationName) {
        return annotationName.equals("javax.ws.rs.PathParam") ||
                annotationName.equals("javax.ws.rs.QueryParam") ||
                annotationName.equals("javax.ws.rs.HeaderParam") ||
                annotationName.equals("javax.ws.rs.CookieParam") ||
                annotationName.equals("javax.ws.rs.MatrixParam") ||
                annotationName.equals("javax.ws.rs.BeanParam") ||
                annotationName.equals("javax.ws.rs.core.Context") ||

                annotationName.equals("jakarta.ws.rs.PathParam") ||
                annotationName.equals("jakarta.ws.rs.QueryParam") ||
                annotationName.equals("jakarta.ws.rs.HeaderParam") ||
                annotationName.equals("jakarta.ws.rs.CookieParam") ||
                annotationName.equals("jakarta.ws.rs.MatrixParam") ||
                annotationName.equals("jakarta.ws.rs.BeanParam") ||
                annotationName.equals("jakarta.ws.rs.core.Context");
    }
}