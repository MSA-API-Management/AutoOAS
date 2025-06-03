package at.aau.serg.frameworks.spring;

import at.aau.serg.frameworks.ValidationAnnotationProvider;

import java.lang.annotation.Annotation;

public class JavaxValidationAnnotationProvider implements ValidationAnnotationProvider {
    @Override
    public Class<?> getPatternClass() {
        return javax.validation.constraints.Pattern.class;
    }

    @Override
    public Class<?> getSizeClass() {
        return javax.validation.constraints.Size.class;
    }

    @Override
    public Class<?> getDecimalMinClass() {
        return javax.validation.constraints.DecimalMin.class;
    }

    @Override
    public Class<?> getDecimalMaxClass() {
        return javax.validation.constraints.DecimalMax.class;
    }

    @Override
    public Class<?> getMinClass() {
        return javax.validation.constraints.Min.class;
    }

    @Override
    public Class<?> getMaxClass() {
        return javax.validation.constraints.Max.class;
    }

    @Override
    public Class<?> getNotNullClass() {
        return javax.validation.constraints.NotNull.class;
    }

    @Override
    public Class<?> getNotEmptyClass() {
        return javax.validation.constraints.NotEmpty.class;
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
