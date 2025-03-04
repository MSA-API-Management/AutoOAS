package at.aau.serg.parsers;

import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;

import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface RestFramework {
    /**
     * Contains the annotation for Spring profiles.
     *
     * @return A string representing the profile annotation for the framework (e.g., "@Profile" for Spring).
     */
    String getProfileAnnotation();

    /**
     * Contains all annotations marking a class as a controller.
     *
     * @return A list of strings representing the annotations for controller classes (e.g., "@RestController", "@Controller").
     */
    List<String> getControllerAnnotations();

    /**
     * Contains all annotations marking a class as a controller advice for exception handling.
     *
     * @return A list of strings representing the annotations for controller advice classes (e.g., "@ControllerAdvice").
     */
    List<String> getControllerAdviceAnnotations();

    /**
     * Contains all annotations marking a class as a controller advice for exception handling.
     *
     * @return A list of strings representing the annotations for model schema classes (e.g., "@Schema").
     */
    List<String> getModelSchemaAnnotations();

    Class<?> getResponseWrapper();

    Class<?> getFileType();

    Class<?> getAsyncResultWrapper();

    Class<?> getParameterGroupAnnotation();

    Optional<Annotation> getPostMapping(CtMethod<?> method);

    Optional<Annotation> getPutMapping(CtMethod<?> method);

    Optional<Annotation> getPatchMapping(CtMethod<?> method);

    Optional<Annotation> getGetMapping(CtMethod<?> method);

    Optional<Annotation> getDeleteMapping(CtMethod<?> method);

    Optional<Annotation> getRequestMapping(CtMethod<?> method);

    Optional<RequestAnnotation> getRequestMapping(CtType<?> clazz);

    String getPathFromAnnotation(Annotation annotation);

    String getNameFromAnnotation(Annotation annotation);

    String getProducesFromAnnotation(Annotation annotation);

    String getConsumesFromAnnotation(Annotation annotation);

    RequestAnnotation getRequestAnnotation(Annotation annotation);

    PathVariableAnnotation getPathVariableAnnotation(CtParameter<?> parameter);

    RequestParamAnnotation getRequestParamAnnotation(CtParameter<?> parameter);

    RequestHeaderAnnotation getRequestHeaderAnnotation(CtParameter<?> parameter);

    default HttpMethod[] getAllSupportedHttpMethods() {
        return HttpMethod.values();
    }

    default boolean isHttpMethodWithRequestBody(HttpMethod... methods) {
        return Stream.of(methods).anyMatch(method ->
                EnumSet.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH).contains(method));
    }
}
