package at.aau.serg.parsers;

import at.aau.serg.frameworks.RestFramework;
import at.aau.serg.frameworks.spring.SpringRestFramework;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ParserFactory {
    private final Map<String, Supplier<RestFramework>> FRAMEWORKS = new HashMap<>();

    public ParserFactory() {
        registerRestFramework("spring", SpringRestFramework::new);
    }

    public void registerRestFramework(String identifier, Supplier<RestFramework> framework) {
        // todo couple identifier to framework parameter
        //  or better, remove from the frameworks' impl because the identifier requires static access
        FRAMEWORKS.put(identifier.toLowerCase(), framework);
    }

    public RestApiParser createParser(String frameworkIdentifier, String projectPath, String outputFileName) {
        Supplier<RestFramework> frameworkSupplier = FRAMEWORKS.get(frameworkIdentifier.toLowerCase());

        if (frameworkSupplier == null) {
            throw new IllegalArgumentException("Unsupported framework: " + frameworkIdentifier);
        }

        return new RestApiParser(projectPath, outputFileName, frameworkSupplier.get());
    }
}
