package at.aau.serg.frameworks;

import java.lang.annotation.Annotation;

public interface ValidationAnnotationProvider {
    Class<?> getPatternClass();

    Class<?> getSizeClass();

    Class<?> getDecimalMinClass();

    Class<?> getDecimalMaxClass();

    Class<?> getMinClass();

    Class<?> getMaxClass();

    Class<?> getNotNullClass();

    Class<?> getNotEmptyClass();

    String getPatternRegexp(Annotation annotation);

    int getSizeMin(Annotation annotation);

    int getSizeMax(Annotation annotation);

    String getDecimalMinValue(Annotation annotation);

    String getDecimalMaxValue(Annotation annotation);

    long getMinValue(Annotation annotation);

    long getMaxValue(Annotation annotation);

}
