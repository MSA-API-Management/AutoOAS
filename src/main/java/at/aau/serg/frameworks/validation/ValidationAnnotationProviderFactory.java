package at.aau.serg.frameworks.validation;

import at.aau.serg.frameworks.ValidationAnnotationProvider;

import java.util.ArrayList;
import java.util.List;

public class ValidationAnnotationProviderFactory {
    private static final List<ValidationAnnotationProvider> providers = new ArrayList<ValidationAnnotationProvider>();
    private static CompositeValidationAnnotationProvider instance;

    static {
        providers.add(new JakartaValidationAnnotationProvider());
        providers.add(new JavaxValidationAnnotationProvider());

        instance = new CompositeValidationAnnotationProvider(new ArrayList<>(providers));
    }

    public static void registerProvider(ValidationAnnotationProvider provider) {
        if (provider != null && !providers.contains(provider)) {
            providers.add(provider);
            instance = new CompositeValidationAnnotationProvider(new ArrayList<>(providers));
        }
    }

    public static CompositeValidationAnnotationProvider getCompositeProvider() {
        return instance;
    }

}
