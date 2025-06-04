package at.aau.serg.frameworks;

import java.lang.annotation.Annotation;

public interface ValidationAnnotationProvider {
    boolean hasNotNullAnnotation(Annotation annotation);

    boolean hasNotEmptyAnnotation(Annotation annotation);

    boolean hasPatternAnnotation(Annotation annotation);

    boolean hasSizeAnnotation(Annotation annotation);

    boolean hasDecimalMinAnnotation(Annotation annotation);

    boolean hasDecimalMaxAnnotation(Annotation annotation);

    boolean hasMinAnnotation(Annotation annotation);

    boolean hasMaxAnnotation(Annotation annotation);

    String getPatternRegexp(Annotation annotation);

    int getSizeMin(Annotation annotation);

    int getSizeMax(Annotation annotation);

    String getDecimalMinValue(Annotation annotation);

    String getDecimalMaxValue(Annotation annotation);

    long getMinValue(Annotation annotation);

    long getMaxValue(Annotation annotation);

}
