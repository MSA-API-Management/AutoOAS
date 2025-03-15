package at.aau.serg.interfaces;

import at.aau.serg.parsers.HttpMethod;
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

    Class<?> getSupportedFileTypes();

    Class<?> getAsyncResultWrapper();

    Class<? extends Annotation> getParameterGroupAnnotation();

    Class<? extends Annotation> getRequestBodyAnnotation();

    Optional<Annotation> getPostMapping(CtMethod<?> method);

    Optional<Annotation> getPutMapping(CtMethod<?> method);

    Optional<Annotation> getPatchMapping(CtMethod<?> method);

    Optional<Annotation> getGetMapping(CtMethod<?> method);

    Optional<Annotation> getDeleteMapping(CtMethod<?> method);

    Optional<Annotation> getRequestMapping(CtMethod<?> method);

    Optional<RequestAnnotation> getRequestMapping(CtType<?> clazz);

    RequestAnnotation convertToRequestAnnotation(Annotation annotation);

    PathVariableAnnotation tryConvertPathVariableAnnotation(CtParameter<?> parameter);

    RequestParamAnnotation tryConvertRequestParamAnnotation(CtParameter<?> parameter);

    RequestHeaderAnnotation tryConvertRequestHeaderAnnotation(CtParameter<?> parameter);

    HttpMethod[] getAllSupportedHttpMethods();

    boolean isHttpMethodWithRequestBody(HttpMethod... methods);
}
