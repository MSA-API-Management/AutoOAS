package at.aau.serg.frameworks;

import java.lang.annotation.Annotation;
import java.util.Optional;

public interface ValidationAnnotationProvider {
    boolean isNotNullAnnotation(Annotation annotation);

    boolean isNotEmptyAnnotation(Annotation annotation);

    Optional<String> getPatternRegexpIfPresent(Annotation annotation);

    Optional<Integer> getSizeMinIfPresent(Annotation annotation);

    Optional<Integer> getSizeMaxIfPresent(Annotation annotation);

    Optional<String> getDecimalMinValueIfPresent(Annotation annotation);

    Optional<String> getDecimalMaxValueIfPresent(Annotation annotation);

    Optional<Long> getMinValueIfPresent(Annotation annotation);

    Optional<Long> getMaxValueIfPresent(Annotation annotation);

}
