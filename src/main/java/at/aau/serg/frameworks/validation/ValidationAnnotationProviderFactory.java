package at.aau.serg.frameworks.validation;

import at.aau.serg.frameworks.ValidationAnnotationProvider;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ValidationAnnotationProviderFactory {
    private final List<ValidationAnnotationProvider> providers = new ArrayList<ValidationAnnotationProvider>();
    @Getter
    private CompositeValidationAnnotationProvider compositeProvider;

    public ValidationAnnotationProviderFactory() {
        providers.add(new JakartaValidationAnnotationProvider());
        providers.add(new JavaxValidationAnnotationProvider());

        updateCompositePattern();
    }

    public void registerProvider(ValidationAnnotationProvider provider) {
        if (provider != null && !providers.contains(provider)) {
            providers.add(provider);
            updateCompositePattern();
        }
    }

    private void updateCompositePattern() {
        compositeProvider = new CompositeValidationAnnotationProvider(new ArrayList<>(providers));
    }
}
