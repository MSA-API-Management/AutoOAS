package at.aau.serg.frameworks.validation;

import at.aau.serg.frameworks.ValidationAnnotationProvider;

import javax.validation.constraints.*;
import java.lang.annotation.Annotation;
import java.util.Optional;

public class JavaxValidationAnnotationProvider implements ValidationAnnotationProvider {
    @Override
    public boolean isNotNullAnnotation(Annotation annotation) {
        return NotNull.class.equals(annotation.annotationType());
    }

    @Override
    public boolean isNotEmptyAnnotation(Annotation annotation) {
        return NotEmpty.class.equals(annotation.annotationType());
    }

    @Override
    public Optional<String> getPatternRegexpIfPresent(Annotation annotation) {
        return Pattern.class.equals(annotation.annotationType()) ? Optional.of(((Pattern) annotation).regexp()) : Optional.empty();
    }

    @Override
    public Optional<Integer> getSizeMinIfPresent(Annotation annotation) {
        return Size.class.equals(annotation.annotationType()) ? Optional.of(((Size) annotation).min()) : Optional.empty();
    }

    @Override
    public Optional<Integer> getSizeMaxIfPresent(Annotation annotation) {
        return Size.class.equals(annotation.annotationType()) ? Optional.of(((Size) annotation).max()) : Optional.empty();
    }

    @Override
    public Optional<String> getDecimalMinValueIfPresent(Annotation annotation) {
        return DecimalMin.class.equals(annotation.annotationType()) ? Optional.of(((DecimalMin) annotation).value()) : Optional.empty();
    }

    @Override
    public Optional<String> getDecimalMaxValueIfPresent(Annotation annotation) {
        return DecimalMax.class.equals(annotation.annotationType()) ? Optional.of(((DecimalMax) annotation).value()) : Optional.empty();
    }

    @Override
    public Optional<Long> getMinValueIfPresent(Annotation annotation) {
        return Min.class.equals(annotation.annotationType()) ? Optional.of(((Min) annotation).value()) : Optional.empty();
    }

    @Override
    public Optional<Long> getMaxValueIfPresent(Annotation annotation) {
        return Max.class.equals(annotation.annotationType()) ? Optional.of(((Max) annotation).value()) : Optional.empty();
    }
}
