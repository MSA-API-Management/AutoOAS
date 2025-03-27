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
     * Represents unique identifier of the implemented framework
     * @return unique framework identifier
     */
    String getIdentifier();

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

    /**
     * Retrieves the annotation class that represents a parameter object as multiple parameters (ie, group) in the framework.
     * @return The annotation class that marks parameter groups in the framework
     */
    Class<? extends Annotation> getParameterGroupAnnotation();

    /**
     * Retrieves the annotation class that represents a request body parameter in the framework.
     * @return The annotation class that marks request body parameters in the framework
     */
    Class<? extends Annotation> getRequestBodyAnnotation();

    // todo concrete annotations with generics?

    /**
     * Retrieves the POST mapping annotation from a method if present.
     *
     * @param method The method to check for POST mapping annotations
     * @return An Optional containing the POST mapping annotation if found, or an empty Optional otherwise
     */
    Optional<Annotation> getPostMapping(CtMethod<?> method);

    /**
     * Retrieves the PUT mapping annotation from a method if present.
     *
     * @param method The method to check for PUT mapping annotations
     * @return An Optional containing the PUT mapping annotation if found, or an empty Optional otherwise
     */
    Optional<Annotation> getPutMapping(CtMethod<?> method);

    /**
     * Retrieves the PATCH mapping annotation from a method if present.
     *
     * @param method The method to check for PATCH mapping annotations
     * @return An Optional containing the PATCH mapping annotation if found, or an empty Optional otherwise
     */
    Optional<Annotation> getPatchMapping(CtMethod<?> method);

    /**
     * Retrieves the GET mapping annotation from a method if present.
     *
     * @param method The method to check for GET mapping annotations
     * @return An Optional containing the GET mapping annotation if found, or an empty Optional otherwise
     */
    Optional<Annotation> getGetMapping(CtMethod<?> method);

    /**
     * Retrieves the DELETE mapping annotation from a method if present.
     *
     * @param method The method to check for DELETE mapping annotations
     * @return An Optional containing the DELETE mapping annotation if found, or an empty Optional otherwise
     */
    Optional<Annotation> getDeleteMapping(CtMethod<?> method);

    /**
     * Retrieves the general request mapping annotation from a method if present.
     *
     * @param method The method to check for request mapping annotations
     * @return An Optional containing the request mapping annotation if found, or an empty Optional otherwise
     */
    Optional<Annotation> getRequestMapping(CtMethod<?> method);

    /**
     * Retrieves the request mapping annotation from a class if present and converts it to a standardized form.
     *
     * @param clazz The class to check for request mapping annotations
     * @return An Optional containing the standardized RequestAnnotation if found, or an empty Optional otherwise
     */
    Optional<RequestAnnotation> getRequestMapping(CtType<?> clazz);

    /**
     * Converts a framework-specific annotation to a standardized RequestAnnotation.
     * @param annotation The framework-specific annotation to convert
     * @return A standardized RequestAnnotation representation of the input annotation
     * @throws IllegalArgumentException if the provided annotation is not supported
     */
    RequestAnnotation convertToRequestAnnotation(Annotation annotation);

    /**
     * Attempts to convert a parameter's path variable annotation to a standardized representation.
     * @param parameter The method parameter to check for path variable annotation
     * @return A standardized PathVariableAnnotation if found, null otherwise
     */
    PathVariableAnnotation tryConvertPathVariableAnnotation(CtParameter<?> parameter);

    /**
     * Attempts to convert a parameter's request parameter annotation to a standardized representation.
     *
     * @param parameter The method parameter to check for request parameter annotations
     * @return A standardized RequestParamAnnotation if found, or null otherwise
     */
    RequestParamAnnotation tryConvertRequestParamAnnotation(CtParameter<?> parameter);

    /**
     * Attempts to convert a parameter's request header annotation to a standardized representation.
     *
     * @param parameter The method parameter to check for request header annotations
     * @return A standardized RequestHeaderAnnotation if found, or null otherwise
     */
    RequestHeaderAnnotation tryConvertRequestHeaderAnnotation(CtParameter<?> parameter);

    /**
     * Retrieves all supported HTTP methods defined in the custom HttpMethod enum.
     * @return An array of all HTTP method constants from the custom HttpMethod enum
     */
    HttpMethod[] getAllSupportedHttpMethods();

    /**
     * Determines whether any of the specified HTTP methods typically include a request body.
     * @param methods One or more HTTP method constants to check
     * @return {@code true} if any of the specified methods typically include a request body,
     *         {@code false} otherwise
     */
    boolean isAnyHttpMethodWithRequestBody(HttpMethod... methods);
}
