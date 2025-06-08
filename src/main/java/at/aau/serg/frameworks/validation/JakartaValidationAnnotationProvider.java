package at.aau.serg.frameworks.validation;

import at.aau.serg.frameworks.ValidationAnnotationProvider;
import jakarta.validation.constraints.*;

import java.lang.annotation.Annotation;
import java.util.Optional;

public class JakartaValidationAnnotationProvider implements ValidationAnnotationProvider {
    @Override
    public boolean hasNotNullAnnotation(Annotation annotation) {
        return annotation.annotationType().equals(javax.validation.constraints.NotNull.class);
    }

    @Override
    public boolean hasNotEmptyAnnotation(Annotation annotation) {
        return annotation.annotationType().equals(javax.validation.constraints.NotEmpty.class);
    }

    @Override
    public Optional<String> getPatternRegexpIfPresent(Annotation annotation) {
        return annotation.annotationType().equals(javax.validation.constraints.Pattern.class) ? Optional.of(((javax.validation.constraints.Pattern) annotation).regexp()) : Optional.empty();
    }

    @Override
    public Optional<Integer> getSizeMinIfPresent(Annotation annotation) {
        return annotation.annotationType().equals(javax.validation.constraints.Size.class) ? Optional.of(((javax.validation.constraints.Size) annotation).min()) : Optional.empty();
    }

    @Override
    public Optional<Integer> getSizeMaxIfPresent(Annotation annotation) {
        return annotation.annotationType().equals(javax.validation.constraints.Size.class) ? Optional.of(((javax.validation.constraints.Size) annotation).max()) : Optional.empty();
    }

    @Override
    public Optional<String> getDecimalMinValueIfPresent(Annotation annotation) {
        return annotation.annotationType().equals(javax.validation.constraints.DecimalMin.class) ? Optional.of(((javax.validation.constraints.DecimalMin) annotation).value()) : Optional.empty();
    }

    @Override
    public Optional<String> getDecimalMaxValueIfPresent(Annotation annotation) {
        return annotation.annotationType().equals(javax.validation.constraints.DecimalMax.class) ? Optional.of(((javax.validation.constraints.DecimalMax) annotation).value()) : Optional.empty();
    }

    @Override
    public Optional<Long> getMinValueIfPresent(Annotation annotation) {
        return annotation.annotationType().equals(javax.validation.constraints.Min.class) ? Optional.of(((javax.validation.constraints.Min) annotation).value()) : Optional.empty();
    }

    @Override
    public Optional<Long> getMaxValueIfPresent(Annotation annotation) {
        return annotation.annotationType().equals(javax.validation.constraints.Max.class) ? Optional.of(((Max) annotation).value()) : Optional.empty();
    }
}
