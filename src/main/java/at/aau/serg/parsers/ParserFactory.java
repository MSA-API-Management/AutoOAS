package at.aau.serg.parsers;

import at.aau.serg.frameworks.RestFramework;
import at.aau.serg.frameworks.spring.SpringRestFramework;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ParserFactory {
    private static final Map<String, Supplier<RestFramework>> FRAMEWORKS = new HashMap<>();

    static {
        FRAMEWORKS.put("spring", SpringRestFramework::new);
    }

    public static void registerFramework(String identifier, Supplier<RestFramework> framework) {
        FRAMEWORKS.put(identifier.toLowerCase(), framework);
    }

    public static RestApiParser createParser(String frameworkIdentifier, String projectPath, String outputFileName) {
        Supplier<RestFramework> frameworkSupplier = FRAMEWORKS.get(frameworkIdentifier.toLowerCase());

        if (frameworkSupplier == null) {
            throw new IllegalArgumentException("Unsupported framework: " + frameworkIdentifier);
        }

        return new RestApiParser(projectPath, outputFileName, frameworkSupplier.get());
    }
}
