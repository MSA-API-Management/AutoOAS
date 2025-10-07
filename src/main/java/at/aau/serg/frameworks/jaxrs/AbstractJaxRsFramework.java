package at.aau.serg.frameworks.jaxrs;

import at.aau.serg.frameworks.RestFramework;
import at.aau.serg.parsers.HttpMethod;
import org.springframework.http.HttpStatus;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

public abstract class AbstractJaxRsFramework implements RestFramework {
    protected boolean exceptionLoggingEnabled;

    @Override
    public String getIdentifier() {
        return "JAX-RS";
    }

    @Override
    public void setExceptionLoggingEnabled(boolean exceptionLoggingEnabled) {
        this.exceptionLoggingEnabled = exceptionLoggingEnabled;
    }

    //  Todo: We do not support profiles for JAX-RS implementations (eg. Quarkus) currently

    @Override
    public Map<String, List<CtType<?>>> splitClassesOnProfiles(List<CtType<?>> controllerClasses) {
        //  Jax-RS does not define any profile functionality
        //  Jersey similarly does not provide profile functionality
        //  Quarkus does, @IfBuildProfile(allOf / anyOf = {"dev","prod"})

        return Map.of("default", controllerClasses);
    }

    @Override
    public String getProfileAnnotation() {
        return "";
    }

    @Override
    public Class<?> getAsyncResultWrapper() {
        return CompletionStage.class;
    }

    @Override
    public Class<? extends Annotation> getRequestBodyAnnotation() {
        return null; // No specific request body annotation exists in JAX-RS
    }

    @Override
    public Optional<? extends Annotation> findRequestMappingAnnotation(CtMethod<?> method) {
        return Optional.empty(); // no generic request mapping exists
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

    protected boolean isParameterTypeString(CtParameter<?> parameter) {
        String parameterType = parameter.getType().getQualifiedName();
        return parameterType.equals("java.lang.String");
    }

    /**
     * Returns true if the parameter is one of the JAX-RS parameters defined via annotation, e.g., {@code @PathParam}.
     *
     * @param parameter
     * @return
     */
    protected boolean isRestMethodAnnotatedParameter(CtParameter<?> parameter) {
        return parameter.getAnnotations().stream()
                .map(CtAnnotation::getAnnotationType)
                .map(CtTypeReference::getQualifiedName)
                .anyMatch(this::isRestParameterAnnotation);
    }

    protected abstract boolean isRestParameterAnnotation(String annotationName);

    /**
     * Returns true if the controller contains any handler method, e.g., annotated with {@code @GET},
     * or sub-resource, e.g., annotated with {@code @Path}.
     *
     * @param controllerType
     * @return
     */
    protected boolean containsHandlerMethodOrSubResource(CtType<?> controllerType) {
        return controllerType.getMethods().stream()
                .anyMatch(method ->
                        method.getAnnotations().stream()
                                .map(CtAnnotation::getAnnotationType)
                                .map(CtTypeReference::getQualifiedName)
                                .anyMatch(this::isRestHandlerMethodOrSubResourceAnnotation)
                );
    }

    protected abstract boolean isRestHandlerMethodOrSubResourceAnnotation(String annotationName);
}
