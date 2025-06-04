package at.aau.serg.frameworks.validation;

import at.aau.serg.frameworks.ValidationAnnotationProvider;

import javax.validation.constraints.*;
import java.lang.annotation.Annotation;

public class JavaxValidationAnnotationProvider implements ValidationAnnotationProvider {
    @Override
    public boolean hasNotNullAnnotation(Annotation annotation) {
        return annotation.annotationType().equals(javax.validation.constraints.NotNull.class);
    }

    @Override
    public boolean hasNotEmptyAnnotation(Annotation annotation) {
        return annotation.annotationType().equals(javax.validation.constraints.NotEmpty.class);
    }

    @Override
    public boolean hasPatternAnnotation(Annotation annotation) {
        return annotation.annotationType().equals(javax.validation.constraints.Pattern.class);
    }

    @Override
    public boolean hasSizeAnnotation(Annotation annotation) {
        return annotation.annotationType().equals(javax.validation.constraints.Size.class);
    }

    @Override
    public boolean hasDecimalMinAnnotation(Annotation annotation) {
        return annotation.annotationType().equals(javax.validation.constraints.DecimalMin.class);
    }

    @Override
    public boolean hasDecimalMaxAnnotation(Annotation annotation) {
        return annotation.annotationType().equals(javax.validation.constraints.DecimalMax.class);
    }

    @Override
    public boolean hasMinAnnotation(Annotation annotation) {
        return annotation.annotationType().equals(javax.validation.constraints.Min.class);
    }

    @Override
    public boolean hasMaxAnnotation(Annotation annotation) {
        return annotation.annotationType().equals(javax.validation.constraints.Max.class);
    }

    @Override
    public String getPatternRegexp(Annotation annotation) {
        return ((javax.validation.constraints.Pattern) annotation).regexp();
    }

    @Override
    public int getSizeMin(Annotation annotation) {
        return ((javax.validation.constraints.Size) annotation).min();
    }

    @Override
    public int getSizeMax(Annotation annotation) {
        return ((javax.validation.constraints.Size) annotation).max();
    }

    @Override
    public String getDecimalMinValue(Annotation annotation) {
        return ((javax.validation.constraints.DecimalMin) annotation).value();
    }

    @Override
    public String getDecimalMaxValue(Annotation annotation) {
        return ((javax.validation.constraints.DecimalMax) annotation).value();
    }

    @Override
    public long getMinValue(Annotation annotation) {
        return ((javax.validation.constraints.Min) annotation).value();
    }

    @Override
    public long getMaxValue(Annotation annotation) {
        return ((javax.validation.constraints.Max) annotation).value();
    }
}
