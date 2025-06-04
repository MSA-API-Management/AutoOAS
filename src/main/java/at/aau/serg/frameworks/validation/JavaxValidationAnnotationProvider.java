package at.aau.serg.frameworks.validation;

import at.aau.serg.frameworks.ValidationAnnotationProvider;

import javax.validation.constraints.*;
import java.lang.annotation.Annotation;

public class JavaxValidationAnnotationProvider implements ValidationAnnotationProvider {
    @Override
    public boolean hasNotNullAnnotation(Annotation annotation) {
        return annotation.annotationType().equals(NotNull.class);
    }

    @Override
    public boolean hasNotEmptyAnnotation(Annotation annotation) {
        return annotation.annotationType().equals(NotEmpty.class);
    }

    @Override
    public boolean hasPatternAnnotation(Annotation annotation) {
        return annotation.annotationType().equals(Pattern.class);
    }

    @Override
    public boolean hasSizeAnnotation(Annotation annotation) {
        return annotation.annotationType().equals(Size.class);
    }

    @Override
    public boolean hasDecimalMinAnnotation(Annotation annotation) {
        return annotation.annotationType().equals(DecimalMin.class);
    }

    @Override
    public boolean hasDecimalMaxAnnotation(Annotation annotation) {
        return annotation.annotationType().equals(DecimalMax.class);
    }

    @Override
    public boolean hasMinAnnotation(Annotation annotation) {
        return annotation.annotationType().equals(Min.class);
    }

    @Override
    public boolean hasMaxAnnotation(Annotation annotation) {
        return annotation.annotationType().equals(Max.class);
    }

    @Override
    public String getPatternRegexp(Annotation annotation) {
        return ((Pattern) annotation).regexp();
    }

    @Override
    public int getSizeMin(Annotation annotation) {
        return ((Size) annotation).min();
    }

    @Override
    public int getSizeMax(Annotation annotation) {
        return ((Size) annotation).max();
    }

    @Override
    public String getDecimalMinValue(Annotation annotation) {
        return ((DecimalMin) annotation).value();
    }

    @Override
    public String getDecimalMaxValue(Annotation annotation) {
        return ((DecimalMax) annotation).value();
    }

    @Override
    public long getMinValue(Annotation annotation) {
        return ((Min) annotation).value();
    }

    @Override
    public long getMaxValue(Annotation annotation) {
        return ((Max) annotation).value();
    }
}
