package at.aau.serg.parsers;

import org.springframework.http.ResponseEntity;
import spoon.reflect.declaration.CtMethod;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

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

    Optional<Annotation> getPostMapping(CtMethod<?> method);
    Optional<Annotation> getPutMapping(CtMethod<?> method);
    Optional<Annotation> getPatchMapping(CtMethod<?> method);
    Optional<Annotation> getGetMapping(CtMethod<?> method);
    Optional<Annotation> getDeleteMapping(CtMethod<?> method);
    Optional<Annotation> getRequestMapping(CtMethod<?> method);

    String getPathFromAnnotation(Annotation annotation);
    String getNameFromAnnotation(Annotation annotation);
    String getProducesFromAnnotation(Annotation annotation);
    String getConsumesFromAnnotation(Annotation annotation);
}
