package at.aau.serg.frameworks.validation;

import at.aau.serg.frameworks.ValidationAnnotationProvider;

import javax.validation.constraints.*;
import java.lang.annotation.Annotation;
import java.util.Optional;

public class JavaxValidationAnnotationProvider implements ValidationAnnotationProvider {
    @Override
    public boolean isNotNullAnnotation(Annotation annotation) {
        return annotation.annotationType().equals(NotNull.class);
    }

    @Override
    public boolean isNotEmptyAnnotation(Annotation annotation) {
        return annotation.annotationType().equals(NotEmpty.class);
    }

    @Override
    public Optional<String> getPatternRegexpIfPresent(Annotation annotation) {
        return annotation.annotationType().equals(Pattern.class) ? Optional.of(((Pattern) annotation).regexp()) : Optional.empty();
    }

    @Override
    public Optional<Integer> getSizeMinIfPresent(Annotation annotation) {
        return annotation.annotationType().equals(Size.class) ? Optional.of(((Size) annotation).min()) : Optional.empty();
    }

    @Override
    public Optional<Integer> getSizeMaxIfPresent(Annotation annotation) {
        return annotation.annotationType().equals(Size.class) ? Optional.of(((Size) annotation).max()) : Optional.empty();
    }

    @Override
    public Optional<String> getDecimalMinValueIfPresent(Annotation annotation) {
        return annotation.annotationType().equals(DecimalMin.class) ? Optional.of(((DecimalMin) annotation).value()) : Optional.empty();
    }

    @Override
    public Optional<String> getDecimalMaxValueIfPresent(Annotation annotation) {
        return annotation.annotationType().equals(DecimalMax.class) ? Optional.of(((DecimalMax) annotation).value()) : Optional.empty();
    }

    @Override
    public Optional<Long> getMinValueIfPresent(Annotation annotation) {
        return annotation.annotationType().equals(Min.class) ? Optional.of(((Min) annotation).value()) : Optional.empty();
    }

    @Override
    public Optional<Long> getMaxValueIfPresent(Annotation annotation) {
        return annotation.annotationType().equals(Max.class) ? Optional.of(((Max) annotation).value()) : Optional.empty();
    }
}
