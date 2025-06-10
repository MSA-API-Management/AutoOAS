package at.aau.serg.frameworks.validation;

import at.aau.serg.frameworks.ValidationAnnotationProvider;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

public class CompositeValidationAnnotationProvider implements ValidationAnnotationProvider {
    private final List<ValidationAnnotationProvider> providers;

    public CompositeValidationAnnotationProvider(List<ValidationAnnotationProvider> providers) {
        this.providers = providers;
    }

    @Override
    public boolean isNotEmptyAnnotation(Annotation annotation) {
        return providers.stream()
                .anyMatch(provider -> provider.isNotEmptyAnnotation(annotation));
    }

    @Override
    public boolean isNotNullAnnotation(Annotation annotation) {
        return providers.stream()
                .anyMatch(provider -> provider.isNotNullAnnotation(annotation));
    }

    @Override
    public Optional<String> getPatternRegexpIfPresent(Annotation annotation) {
        return providers.stream()
                .map(provider -> provider.getPatternRegexpIfPresent(annotation))
                .flatMap(Optional::stream)
                .findFirst();
    }

    @Override
    public Optional<Integer> getSizeMinIfPresent(Annotation annotation) {
        return providers.stream()
                .map(provider -> provider.getSizeMinIfPresent(annotation))
                .flatMap(Optional::stream)
                .findFirst();
    }

    @Override
    public Optional<Integer> getSizeMaxIfPresent(Annotation annotation) {
        return providers.stream()
                .map(provider -> provider.getSizeMaxIfPresent(annotation))
                .flatMap(Optional::stream)
                .findFirst();
    }

    @Override
    public Optional<String> getDecimalMinValueIfPresent(Annotation annotation) {
        return providers.stream()
                .map(provider -> provider.getDecimalMinValueIfPresent(annotation))
                .flatMap(Optional::stream)
                .findFirst();
    }

    @Override
    public Optional<String> getDecimalMaxValueIfPresent(Annotation annotation) {
        return providers.stream()
                .map(provider -> provider.getDecimalMaxValueIfPresent(annotation))
                .flatMap(Optional::stream)
                .findFirst();
    }

    @Override
    public Optional<Long> getMinValueIfPresent(Annotation annotation) {
        return providers.stream()
                .map(provider -> provider.getMinValueIfPresent(annotation))
                .flatMap(Optional::stream)
                .findFirst();
    }

    @Override
    public Optional<Long> getMaxValueIfPresent(Annotation annotation) {
        return providers.stream()
                .map(provider -> provider.getMaxValueIfPresent(annotation))
                .flatMap(Optional::stream)
                .findFirst();
    }
}
