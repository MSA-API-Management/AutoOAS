package at.aau.serg.frameworks.utils;

import spoon.reflect.declaration.CtMethod;

import java.lang.annotation.Annotation;
import java.util.Optional;

public class AnnotationUtils {
    public static <T extends Annotation> Optional<T> getAnnotation(CtMethod<?> method, Class<T> annotationClass) {
        return Optional.ofNullable(method.getAnnotation(annotationClass));
    }
}
