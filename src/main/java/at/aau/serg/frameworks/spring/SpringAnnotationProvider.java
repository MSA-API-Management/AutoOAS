package at.aau.serg.frameworks.spring;

import at.aau.serg.frameworks.AnnotationProvider;

import java.lang.annotation.Annotation;

public class SpringAnnotationProvider implements AnnotationProvider {
    @Override
    public Class<?> getPatternClass() {
        return null;
    }

    @Override
    public Class<?> getSizeClass() {
        return null;
    }

    @Override
    public Class<?> getDecimalMinClass() {
        return null;
    }

    @Override
    public Class<?> getDecimalMaxClass() {
        return null;
    }

    @Override
    public Class<?> getMinClass() {
        return null;
    }

    @Override
    public Class<?> getMaxClass() {
        return null;
    }

    @Override
    public String getPatternRegexp(Annotation annotation) {
        return "";
    }

    @Override
    public int getSizeMin(Annotation annotation) {
        return 0;
    }

    @Override
    public int getSizeMax(Annotation annotation) {
        return 0;
    }

    @Override
    public String getDecimalMinValue(Annotation annotation) {
        return "";
    }

    @Override
    public String getDecimalMaxValue(Annotation annotation) {
        return "";
    }

    @Override
    public long getMinValue(Annotation annotation) {
        return 0;
    }

    @Override
    public long getMaxValue(Annotation annotation) {
        return 0;
    }
}
