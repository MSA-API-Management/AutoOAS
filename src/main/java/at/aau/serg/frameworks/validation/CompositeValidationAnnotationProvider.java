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
    public boolean hasNotEmptyAnnotation(Annotation annotation) {
        return providers.stream()
                .anyMatch(provider -> provider.hasNotEmptyAnnotation(annotation));
    }

    @Override
    public boolean hasNotNullAnnotation(Annotation annotation) {
        return providers.stream()
                .anyMatch(provider -> provider.hasNotNullAnnotation(annotation));
    }

    @Override
    public Optional<String> getPatternRegexpIfPresent(Annotation annotation) {
        return providers.stream()
                .map(provider -> provider.getPatternRegexpIfPresent(annotation))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }

    @Override
    public Optional<Integer> getSizeMinIfPresent(Annotation annotation) {
        return providers.stream()
                .map(provider -> provider.getSizeMinIfPresent(annotation))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }

    @Override
    public Optional<Integer> getSizeMaxIfPresent(Annotation annotation) {
        return providers.stream()
                .map(provider -> provider.getSizeMaxIfPresent(annotation))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }

    @Override
    public Optional<String> getDecimalMinValueIfPresent(Annotation annotation) {
        return providers.stream()
                .map(provider -> provider.getDecimalMinValueIfPresent(annotation))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }

    @Override
    public Optional<String> getDecimalMaxValueIfPresent(Annotation annotation) {
        return providers.stream()
                .map(provider -> provider.getDecimalMaxValueIfPresent(annotation))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }

    @Override
    public Optional<Long> getMinValueIfPresent(Annotation annotation) {
        return providers.stream()
                .map(provider -> provider.getMinValueIfPresent(annotation))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }

    @Override
    public Optional<Long> getMaxValueIfPresent(Annotation annotation) {
        return providers.stream()
                .map(provider -> provider.getMaxValueIfPresent(annotation))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }
}
