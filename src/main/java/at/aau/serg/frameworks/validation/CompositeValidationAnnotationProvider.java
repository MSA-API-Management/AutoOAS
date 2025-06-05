package at.aau.serg.frameworks.validation;

import at.aau.serg.frameworks.ValidationAnnotationProvider;

import java.lang.annotation.Annotation;
import java.util.List;

public class CompositeValidationAnnotationProvider implements ValidationAnnotationProvider {
    // TODO throw exception when more than one provider was found
    private final List<ValidationAnnotationProvider> providers;

    public CompositeValidationAnnotationProvider(List<ValidationAnnotationProvider> providers) {
        this.providers = providers;
    }

    @Override
    public boolean hasNotNullAnnotation(Annotation annotation) {
        return providers.stream().anyMatch(provider -> provider.hasNotNullAnnotation(annotation));
    }

    @Override
    public boolean hasNotEmptyAnnotation(Annotation annotation) {
        return providers.stream().anyMatch(provider -> provider.hasNotEmptyAnnotation(annotation));
    }

    @Override
    public boolean hasPatternAnnotation(Annotation annotation) {
        return providers.stream().anyMatch(provider -> provider.hasPatternAnnotation(annotation));
    }

    @Override
    public boolean hasSizeAnnotation(Annotation annotation) {
        return providers.stream().anyMatch(provider -> provider.hasSizeAnnotation(annotation));
    }

    @Override
    public boolean hasDecimalMinAnnotation(Annotation annotation) {
        return providers.stream().anyMatch(provider -> provider.hasDecimalMinAnnotation(annotation));
    }

    @Override
    public boolean hasDecimalMaxAnnotation(Annotation annotation) {
        return providers.stream().anyMatch(provider -> provider.hasDecimalMaxAnnotation(annotation));
    }

    @Override
    public boolean hasMinAnnotation(Annotation annotation) {
        return providers.stream().anyMatch(provider -> provider.hasMinAnnotation(annotation));
    }

    @Override
    public boolean hasMaxAnnotation(Annotation annotation) {
        return providers.stream().anyMatch(provider -> provider.hasMaxAnnotation(annotation));
    }

    @Override
    public String getPatternRegexp(Annotation annotation) {
        return providers.stream()
                .filter(provider -> provider.hasPatternAnnotation(annotation))
                .findFirst()
                .map(provider -> provider.getPatternRegexp(annotation))
                .orElseThrow(() -> new IllegalArgumentException("No provider found for Pattern annotation"));
    }

    @Override
    public int getSizeMin(Annotation annotation) {
        return providers.stream()
                .filter(provider -> provider.hasSizeAnnotation(annotation))
                .findFirst()
                .map(provider -> provider.getSizeMin(annotation))
                .orElseThrow(() -> new IllegalArgumentException("No provider found for Size annotation"));
    }

    @Override
    public int getSizeMax(Annotation annotation) {
        return providers.stream()
                .filter(provider -> provider.hasSizeAnnotation(annotation))
                .findFirst()
                .map(provider -> provider.getSizeMax(annotation))
                .orElseThrow(() -> new IllegalArgumentException("No provider found for Size annotation"));
    }

    @Override
    public String getDecimalMinValue(Annotation annotation) {
        return providers.stream()
                .filter(provider -> provider.hasDecimalMinAnnotation(annotation))
                .findFirst()
                .map(provider -> provider.getDecimalMinValue(annotation))
                .orElseThrow(() -> new IllegalArgumentException("No provider found for DecimalMin annotation"));
    }

    @Override
    public String getDecimalMaxValue(Annotation annotation) {
        return providers.stream()
                .filter(provider -> provider.hasDecimalMaxAnnotation(annotation))
                .findFirst()
                .map(provider -> provider.getDecimalMaxValue(annotation))
                .orElseThrow(() -> new IllegalArgumentException("No provider found for DecimalMax annotation"));
    }

    @Override
    public long getMinValue(Annotation annotation) {
        return providers.stream()
                .filter(provider -> provider.hasMinAnnotation(annotation))
                .findFirst()
                .map(provider -> provider.getMinValue(annotation))
                .orElseThrow(() -> new IllegalArgumentException("No provider found for Min annotation"));
    }

    @Override
    public long getMaxValue(Annotation annotation) {
        return providers.stream()
                .filter(provider -> provider.hasMaxAnnotation(annotation))
                .findFirst()
                .map(provider -> provider.getMaxValue(annotation))
                .orElseThrow(() -> new IllegalArgumentException("No provider found for Max annotation"));
    }
}
