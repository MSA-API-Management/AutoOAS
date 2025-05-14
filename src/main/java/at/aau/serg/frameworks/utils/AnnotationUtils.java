package at.aau.serg.frameworks.utils;

import spoon.reflect.declaration.CtMethod;

import java.lang.annotation.Annotation;
import java.util.Optional;

public class AnnotationUtils {
    public static Optional<Annotation> getAnnotation(CtMethod<?> method, Class<? extends Annotation> annotationClass) {
        return Optional.ofNullable(method.getAnnotation(annotationClass));
    }
}
