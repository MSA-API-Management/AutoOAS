package at.aau.serg.frameworks.jakarta;

import at.aau.serg.frameworks.*;
import at.aau.serg.frameworks.jakarta.adapters.mappings.JakartaRestOperationAnnotationAdapter;
import at.aau.serg.frameworks.jakarta.adapters.parameters.*;
import at.aau.serg.frameworks.validation.ValidationAnnotationProviderFactory;
import at.aau.serg.interceptors.JakartaOperationResponseCodeInterceptor;
import at.aau.serg.parsers.HttpMethod;
import com.github.jrcodeza.schema.generator.DataTypeTransformer;
import com.github.jrcodeza.schema.generator.MethodResponseExtractor;
import com.github.jrcodeza.schema.generator.interceptors.OperationInterceptor;
import com.github.jrcodeza.schema.generator.util.SchemaGeneratorHelper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.springframework.http.HttpStatus;
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

// todo consider splitting javax and jakarta namespaces into two frameworks
public class JakartaRestFramework implements RestFramework {
    ValidationAnnotationProvider validationAnnotationProvider = new ValidationAnnotationProviderFactory().getCompositeProvider();

    @Override
    public String getIdentifier() {
        return "Jakarta";
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
        return List.of(
                "jakarta.ws.rs.Path",
                "javax.ws.rs.Path"
        );
    }

    /**
     * An exception contains @Provider as annotation and also
     * implements the jakarta.ws.rs.ext.ExceptionMapper
     */
    @Override
    public List<String> getControllerAdviceAnnotations() {
        return List.of(
                "jakarta.ws.rs.ext.Provider",
                "javax.ws.rs.ext.Provider"
        );
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

    // todo what about javax namespace
    @Override
    public Class<?> getResponseWrapper() {
        return Response.class;
    }

    // todo what about javax namespace
    @Override
    public Class<?> getSupportedFileType() {
        return MultivaluedMap.class;
    }

    @Override
    public Class<?> getAsyncResultWrapper() {
        return CompletionStage.class;
    }

    // todo what about javax namespace
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
        return getAnnotation(method, POST.class)
                .or(() -> getAnnotation(method, javax.ws.rs.POST.class));
    }

    @Override
    public Optional<Annotation> findPutMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, PUT.class)
                .or(() -> getAnnotation(method, javax.ws.rs.PUT.class));
    }

    @Override
    public Optional<Annotation> findPatchMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, PATCH.class)
                .or(() -> getAnnotation(method, javax.ws.rs.PATCH.class));
    }

    @Override
    public Optional<Annotation> findGetMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, GET.class)
                .or(() -> getAnnotation(method, javax.ws.rs.GET.class));
    }

    @Override
    public Optional<Annotation> findDeleteMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, DELETE.class)
                .or(() -> getAnnotation(method, javax.ws.rs.DELETE.class));
    }

    @Override
    public Optional<Annotation> findRequestMappingAnnotation(CtMethod<?> method) {
        return Optional.empty();    // no request mapping exists
    }

    @Override
    public Optional<RestOperationAnnotation> findClassRequestMappingAnnotation(CtType<?> clazz) {
        for (CtAnnotation<?> annotation : clazz.getAnnotations()) {
            String annotationTypeName = annotation.getAnnotationType().getQualifiedName();
            if (annotationTypeName.equals(Path.class.getName())
                    || annotationTypeName.equals(javax.ws.rs.Path.class.getName())) {
                return Optional.of(new JakartaRestOperationAnnotationAdapter(clazz));
            }
        }
        return Optional.empty();
    }

    @Override
    public RestOperationAnnotation convertToRequestAnnotation(Annotation annotation, CtMethod<?> method) {
        if (annotation instanceof POST || annotation instanceof javax.ws.rs.POST) {
            return new JakartaRestOperationAnnotationAdapter(method, HttpMethod.POST);
        }
        if (annotation instanceof PUT || annotation instanceof javax.ws.rs.PUT) {
            return new JakartaRestOperationAnnotationAdapter(method, HttpMethod.PUT);
        }
        if (annotation instanceof PATCH || annotation instanceof javax.ws.rs.PATCH) {
            return new JakartaRestOperationAnnotationAdapter(method, HttpMethod.PATCH);
        }
        if (annotation instanceof GET || annotation instanceof javax.ws.rs.GET) {
            return new JakartaRestOperationAnnotationAdapter(method, HttpMethod.GET);
        }
        if (annotation instanceof DELETE || annotation instanceof javax.ws.rs.DELETE) {
            return new JakartaRestOperationAnnotationAdapter(method, HttpMethod.DELETE);
        }

        throw new IllegalArgumentException("No supported annotation found");
    }

    @Override
    public PathVariableAnnotation tryConvertPathVariableAnnotation(CtParameter<?> parameter) {
        PathParam annotation = parameter.getAnnotation(PathParam.class);
        if (annotation != null) {
            return new JakartaPathParamAdapter(annotation);
        }

        var legacyAnnotation = parameter.getAnnotation(javax.ws.rs.PathParam.class);
        if (legacyAnnotation != null) {
            return new JavaxPathParamAdapter(legacyAnnotation);
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
            JakartaQueryParamAdapter adapter = new JakartaQueryParamAdapter(annotation);
            adapter.setRequired(isParameterRequired);
            return adapter;
        }

        var legacyAnnotation = parameter.getAnnotation(javax.ws.rs.QueryParam.class);
        if (legacyAnnotation != null) {
            var adapter = new JavaxQueryParamAdapter(legacyAnnotation);
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
            JakartaHeaderParamAdapter adapter = new JakartaHeaderParamAdapter(annotation);
            adapter.setRequired(isParameterRequired);
            return adapter;
        }

        var legacyAnnotation = parameter.getAnnotation(javax.ws.rs.HeaderParam.class);
        if (legacyAnnotation != null) {
            JavaxHeaderParamAdapter adapter = new JavaxHeaderParamAdapter(legacyAnnotation);
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