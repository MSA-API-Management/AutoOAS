package at.aau.serg.parsers;

import at.aau.serg.frameworks.RestFramework;
import at.aau.serg.frameworks.jakarta.JakartaRestFramework;
import at.aau.serg.frameworks.spring.SpringRestFramework;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ParserFactory {
    private final Map<String, Supplier<RestFramework>> FRAMEWORKS = new HashMap<>();
    private final FrameworkDetector frameworkDetector = new FrameworkDetector();

    public ParserFactory() {
        registerRestFramework("spring", SpringRestFramework::new);
        registerRestFramework("jakarta", JakartaRestFramework::new);
    }

    public void registerRestFramework(String identifier, Supplier<RestFramework> framework) {
        // todo couple identifier to framework parameter
        //  or better, remove from the frameworks' impl because the identifier requires static access
        FRAMEWORKS.put(identifier.toLowerCase(), framework);
    }

    public void addFrameworkDetection(RestFramework framework, Set<String> keyAnnotations) {
        frameworkDetector.addFramework(framework, keyAnnotations);
    }

    private RestApiParser createParser(String frameworkIdentifier, String projectPath, String outputFileName) {

        Supplier<RestFramework> frameworkSupplier = FRAMEWORKS.get(frameworkIdentifier.toLowerCase());

        if (frameworkSupplier == null) {
            throw new IllegalArgumentException("Unsupported framework: " + frameworkIdentifier);
        }

        return new RestApiParser(projectPath, outputFileName, frameworkSupplier.get(), frameworkDetector.getModel());
    }

    public RestApiParser createParserWithDetection(String projectPath, String outputFileName) {
        RestFramework detectedFramework = frameworkDetector.detectFramework(projectPath);

        if (detectedFramework == null) {
            // fall back to manual framework if no framework was found
            // todo needs to be replaced with exception
            return createParser("jakarta", projectPath, outputFileName);
        }

        return new RestApiParser(projectPath, outputFileName, detectedFramework, frameworkDetector.getModel());
    }
}
