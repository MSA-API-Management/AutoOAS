package at.aau.serg.frameworks;

import at.aau.serg.parsers.HttpMethod;
import com.github.jrcodeza.schema.generator.DataTypeTransformer;
import com.github.jrcodeza.schema.generator.MethodResponseExtractor;
import com.github.jrcodeza.schema.generator.interceptors.OperationInterceptor;
import com.github.jrcodeza.schema.generator.util.SchemaGeneratorHelper;
import org.springframework.http.HttpStatus;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface RestFramework {
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
     * @param globalExceptionHandlerClasses A list of controller advice or exception mapper classes that handle exceptions
     *                                      and define response codes for the REST API
     * @param dataTypeTransformer
     * @return An implementation of OperationResponseCodeInterceptor specific to the REST framework
     */
    OperationInterceptor getOperationResponseCodeInterceptor(List<CtType<?>> globalExceptionHandlerClasses,
                                                             DataTypeTransformer dataTypeTransformer,
                                                             SchemaGeneratorHelper schemaHelper,
                                                             MethodResponseExtractor methodResponseExtractor);

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
     * Returns a list of annotations that indicate a specific Rest Framework.
     *
     * <p>Examples: </p>
     * <pre> {@code
     * // Spring annotation
     * import org.springframework.web.bind.annotation.RestController;
     * @RestController()
     *
     * // Jakarta annotation
     * import jakarta.ws.rs.Path;
     * @Path()
     * } </pre>
     *
     * @return A list of annotations indicating a specific Rest Framework
     */
    Set<String> getKeyAnnotations();

    /**
     * Determines if a class type represents a global exception handler for this REST framework
     *
     * @param annotationName the name of the annotation found on the type
     * @param type           the class type to check
     * @return true if the type is a global exception handler, false otherwise
     */
    boolean isGlobalExceptionHandler(String annotationName, CtType<?> type);

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

    /**
     * Returns the first parameter of a parameter list that is the RequestBody
     *
     * @param parameters list of parameters
     * @return parameter that is the RequestBody or null if no RequestBody could be found
     */
    CtParameter<?> findRequestBody(List<CtParameter<?>> parameters);


// endregion framework-specific classes

// region framework-specific REST functionality conversions
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

    default HttpStatus getDefaultResponseCode() {
        return HttpStatus.OK;
    }

    /**
     * Returns the HTTP status code for handler methods returning void, e.g., 204 or 200.
     *
     * @return
     */
    HttpStatus getVoidMethodStatusCode();
}
