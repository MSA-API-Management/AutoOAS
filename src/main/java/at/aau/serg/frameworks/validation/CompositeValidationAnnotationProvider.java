package at.aau.serg.frameworks.validation;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

public class CompositeValidationAnnotationProvider implements ValidationAnnotationProvider {
    // TODO throw exception when more than one provider was found
    private final List<ValidationAnnotationProvider> providers;

    public CompositeValidationAnnotationProvider(List<ValidationAnnotationProvider> providers) {
        this.providers = providers;
    }

    public Optional<String> getPatternRegexpIfPresent(Annotation annotation) {
        return providers.stream()
                .map(provider -> provider.getPatternRegexpIfPresent(annotation))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }


    public Optional<Integer> getSizeMinIfPresent(Annotation annotation) {
        return providers.stream()
                .map(provider -> provider.getSizeMinIfPresent(annotation))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }


    public Optional<Integer> getSizeMaxIfPresent(Annotation annotation) {
        return providers.stream()
                .map(provider -> provider.getSizeMaxIfPresent(annotation))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }


    public Optional<String> getDecimalMinValueIfPresent(Annotation annotation) {
        return providers.stream()
                .map(provider -> provider.getDecimalMinValueIfPresent(annotation))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }


    public Optional<String> getDecimalMaxValueIfPresent(Annotation annotation) {
        return providers.stream()
                .map(provider -> provider.getDecimalMaxValueIfPresent(annotation))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }


    public Optional<Long> getMinValueIfPresent(Annotation annotation) {
        return providers.stream()
                .map(provider -> provider.getMinValueIfPresent(annotation))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }


    public Optional<Long> getMaxValueIfPresent(Annotation annotation) {
        return providers.stream()
                .map(provider -> provider.getMaxValueIfPresent(annotation))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }


    public boolean hasNotEmptyAnnotation(Annotation annotation) {
        return providers.stream()
                .anyMatch(provider -> provider.hasNotEmptyAnnotation(annotation));
    }

    public boolean hasNotNullAnnotation(Annotation annotation) {
        return providers.stream()
                .anyMatch(provider -> provider.hasNotNullAnnotation(annotation));
    }
}
