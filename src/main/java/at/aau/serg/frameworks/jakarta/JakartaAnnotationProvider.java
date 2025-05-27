package at.aau.serg.frameworks.jakarta;

import java.lang.annotation.Annotation;

public class JakartaAnnotationProvider implements at.aau.serg.frameworks.AnnotationProvider {
    // TODO also add javax?
    @Override
    public Class<?> getPatternClass() {
        return jakarta.validation.constraints.Pattern.class;
    }

    @Override
    public Class<?> getSizeClass() {
        return jakarta.validation.constraints.Size.class;
    }

    @Override
    public Class<?> getDecimalMinClass() {
        return jakarta.validation.constraints.DecimalMin.class;
    }

    @Override
    public Class<?> getDecimalMaxClass() {
        return jakarta.validation.constraints.DecimalMax.class;
    }

    @Override
    public Class<?> getMinClass() {
        return jakarta.validation.constraints.Min.class;
    }

    @Override
    public Class<?> getMaxClass() {
        return jakarta.validation.constraints.Max.class;
    }

    @Override
    public String getPatternRegexp(Annotation annotation) {
        return ((jakarta.validation.constraints.Pattern) annotation).regexp();
    }

    @Override
    public int getSizeMin(Annotation annotation) {
        return ((jakarta.validation.constraints.Size) annotation).min();
    }

    @Override
    public int getSizeMax(Annotation annotation) {
        return ((jakarta.validation.constraints.Size) annotation).max();
    }

    @Override
    public String getDecimalMinValue(Annotation annotation) {
        return ((jakarta.validation.constraints.DecimalMin) annotation).value();
    }

    @Override
    public String getDecimalMaxValue(Annotation annotation) {
        return ((jakarta.validation.constraints.DecimalMax) annotation).value();
    }

    @Override
    public long getMinValue(Annotation annotation) {
        return ((jakarta.validation.constraints.Min) annotation).value();
    }

    @Override
    public long getMaxValue(Annotation annotation) {
        return ((jakarta.validation.constraints.Max) annotation).value();
    }
}
