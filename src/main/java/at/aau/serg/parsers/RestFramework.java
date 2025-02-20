package at.aau.serg.parsers;

import org.springframework.http.ResponseEntity;

import java.util.List;

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
}
