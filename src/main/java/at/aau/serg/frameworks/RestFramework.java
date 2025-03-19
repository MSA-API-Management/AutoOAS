package at.aau.serg.frameworks;

import at.aau.serg.parsers.HttpMethod;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

public interface RestFramework {
    /**
     * Returns the fully qualified class name of the Profile annotation for the current framework.
     *
     * @return The fully qualified class name of the Profile annotation used by the framework
     */
    String getProfileAnnotation();

    /**
     * Returns a list of fully qualified class names for annotations that mark a class
     * as a web controller in the current framework.
     *
     * @return A list of fully qualified annotation class names for controller classes
     */
    List<String> getControllerAnnotations();

    /**
     * Returns a list of fully qualified class names for annotations that mark a class
     * as a controller advice for global exception handling.
     *
     * @return A list of fully qualified annotation class names for controller advice classes
     */
    List<String> getControllerAdviceAnnotations();

    /**
     * Returns a list of fully qualified class names for annotations that mark a class
     * as a model schema for API documentation.
     *
     * @return A list of fully qualified annotation class names for model schema classes
     */
    List<String> getModelSchemaAnnotations();

    /**
     * Returns the class that wraps HTTP responses in the current framework.
     *
     * @return The class type used for wrapping HTTP responses
     */
    Class<?> getResponseWrapper();

    /**
     * Returns the class used to represent uploaded files in the current framework.
     *
     * @return The class type used for handling uploaded files
     */
    Class<?> getSupportedFileType();

    /**
     * Returns the class used for handling asynchronous results in the current framework.
     *
     * @return The class type used for asynchronous result handling
     */
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
