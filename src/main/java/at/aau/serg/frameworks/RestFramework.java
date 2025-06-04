package at.aau.serg.frameworks;

import at.aau.serg.frameworks.validation.CompositeValidationAnnotationProvider;
import at.aau.serg.frameworks.validation.JakartaValidationAnnotationProvider;
import at.aau.serg.frameworks.validation.JavaxValidationAnnotationProvider;
import at.aau.serg.parsers.HttpMethod;
import com.github.jrcodeza.schema.generator.interceptors.OperationInterceptor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RestFramework {
    default CompositeValidationAnnotationProvider getValidationAnnotationProvider() {
        return new CompositeValidationAnnotationProvider(Arrays.asList(new JakartaValidationAnnotationProvider(), new JavaxValidationAnnotationProvider()));
    }

    /**
     * Returns the unique identifier of the implemented framework
     *
     * @return unique framework identifier
     */
    String getIdentifier();

    /**
     * Returns a framework specific description for the OpenAPI documentation.
     *
     * @param profileName The profile name (if applicable to the framework)
     * @return A properly formatted description string
     */
    default String getOpenApiInfoDescription(String profileName) {
        return String.format("%s Profile: %s", getIdentifier(), profileName);
    }

    /**
     * Returns the appropriate response code interceptor implementation for the specific REST framework.
     *
     * @param adviceClasses A list of controller advice or exception mapper classes that handle exceptions
     *                      and define response codes for the REST API TODO check equivalent
     * @return An implementation of OperationResponseCodeInterceptor specific to the REST framework
     */
    OperationInterceptor getOperationResponseCodeInterceptor(List<CtType<?>> adviceClasses);

    /**
     * Splits all detected controller classes into potentially overlapping sets of controller classes based on their profile assignment.
     * If a framework does not support runtime configuration profiles it should return the whole set of classes as the "default" profile.
     *
     * @param controllerClasses a set of all controller classes
     * @return A map of profile names to the (not necessarily disjoint) subset of controller classes for that profile
     */
    Map<String, List<CtType<?>>> splitClassesOnProfiles(List<CtType<?>> controllerClasses);


// region framework-specific classes

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
     * Returns the annotation class that represents a parameter object as multiple parameters (ie, group) in the framework.
     *
     * @return The annotation class that marks parameter groups in the framework
     */
    Class<? extends Annotation> getParameterGroupAnnotation();

    /**
     * Returns the annotation class that represents a request body parameter in the framework.
     *
     * @return The annotation class that marks request body parameters in the framework
     */
    Class<? extends Annotation> getRequestBodyAnnotation();

// endregion framework-specific classes

// region framework-specific REST functionality conversions

    /**
     * Determines if the framework has a request body annotation
     *
     * @return true if request body annotation exists else false
     */
    default boolean hasRequestBodyAnnotation() {
        return getRequestBodyAnnotation() != null;
    }

    // todo concrete annotations with generics?

    /**
     * Retrieves the POST mapping annotation from a method if present.
     *
     * @param method The method to search for POST mapping annotations
     * @return An Optional containing the POST mapping annotation if found, or an empty Optional otherwise
     */
    Optional<Annotation> findPostMappingAnnotation(CtMethod<?> method);

    /**
     * Retrieves the PUT mapping annotation from a method if present.
     *
     * @param method The method to search for PUT mapping annotations
     * @return An Optional containing the PUT mapping annotation if found, or an empty Optional otherwise
     */
    Optional<Annotation> findPutMappingAnnotation(CtMethod<?> method);

    /**
     * Retrieves the PATCH mapping annotation from a method if present.
     *
     * @param method The method to search for PATCH mapping annotations
     * @return An Optional containing the PATCH mapping annotation if found, or an empty Optional otherwise
     */
    Optional<Annotation> findPatchMappingAnnotation(CtMethod<?> method);

    /**
     * Retrieves the GET mapping annotation from a method if present.
     *
     * @param method The method to search for GET mapping annotations
     * @return An Optional containing the GET mapping annotation if found, or an empty Optional otherwise
     */
    Optional<Annotation> findGetMappingAnnotation(CtMethod<?> method);

    /**
     * Retrieves the DELETE mapping annotation from a method if present.
     *
     * @param method The method to search for DELETE mapping annotations
     * @return An Optional containing the DELETE mapping annotation if found, or an empty Optional otherwise
     */
    Optional<Annotation> findDeleteMappingAnnotation(CtMethod<?> method);

    /**
     * Retrieves a generic request mapping annotation from a method if present.
     *
     * @param method The method to search for request mapping annotations
     * @return An Optional containing the request mapping annotation if found, or an empty Optional otherwise
     */
    Optional<Annotation> findRequestMappingAnnotation(CtMethod<?> method);

    /**
     * Retrieves and converts a generic request mapping annotation from a class to a standardized form if present.
     *
     * @param clazz The class to search for request mapping annotations
     * @return An Optional containing the standardized RequestAnnotation if found, or an empty Optional otherwise
     */
    Optional<RestOperationAnnotation> findClassRequestMappingAnnotation(CtType<?> clazz);

    /**
     * Converts a framework-specific annotation to a standardized RequestAnnotation.
     *
     * @param annotation The framework-specific annotation to convert
     * @param method     Currently parsed method to extract annotations
     * @return A standardized RequestAnnotation representation of the input annotation
     * @throws IllegalArgumentException if the provided annotation is not supported
     */
    RestOperationAnnotation convertToRequestAnnotation(Annotation annotation, CtMethod<?> method);

    /**
     * Attempts to convert a parameter's path variable annotation to a standardized representation.
     *
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

// endregion framework-specific REST functionality conversions


    /**
     * Retrieves all supported HTTP methods defined in the custom HttpMethod enum.
     *
     * @return An array of all HTTP method constants from the custom HttpMethod enum
     */
    HttpMethod[] getAllSupportedHttpMethods();

    /**
     * Determines whether any of the specified HTTP methods typically include a request body.
     *
     * @param methods One or more HTTP method constants to check
     * @return {@code true} if any of the specified methods typically include a request body,
     * {@code false} otherwise
     */
    boolean isAnyHttpMethodWithRequestBody(HttpMethod... methods);

    /**
     * Checks if a type represents a REST framework context injection object.
     * Examples include:
     * <br>- javax.ws.rs.core.SecurityContext
     * <br>- jakarta.ws.rs.core.HttpHeaders
     *
     * @param type The type reference to check
     * @return true if it is a framework-injected context object
     */
    boolean isRestFrameworkInjectedType(CtTypeReference<?> type);

    /**
     * Determines if a method parameter is annotated with any REST annotation that indicates
     * the parameter should be bound from a non-body source.
     * Examples (here: jakarta) include:
     * <br> - @PathParam (path variables)
     * <br>- @QueryParam (URL query parameters)
     * <br>- @HeaderParam (Http headers)
     * <br>- @Context (framework context objects)
     * ...
     * <p>
     * Note: This method specifically checks for REST binding annotations, not
     * validation annotations like @NotNull.
     * <p>
     * This is primarily needed for JAX-RS frameworks where parameters without binding
     * annotations are implicitly bound to the request body. Spring Framework doesn't
     * require this check since it uses explicit @RequestBody annotations for body binding
     *
     * @param parameter The method parameter to check
     * @return true if annotated with any non-body binding annotation
     */
    boolean hasRestParameterBindingAnnotation(CtParameter<?> parameter);
}
