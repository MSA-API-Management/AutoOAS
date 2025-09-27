package at.aau.serg.parsers;

import at.aau.serg.frameworks.RestFramework;
import at.aau.serg.frameworks.jaxrs.JakartaRestFramework;
import at.aau.serg.frameworks.jaxrs.JavaxRestFramework;
import at.aau.serg.frameworks.spring.SpringRestFramework;

import java.util.HashMap;
import java.util.Map;

public class ParserFactory {
    private final Map<String, RestFramework> frameworkInstances = new HashMap<>();

    public ParserFactory() {
        registerRestFramework(new SpringRestFramework());
        registerRestFramework(new JakartaRestFramework());
        registerRestFramework(new JavaxRestFramework());
    }

    public void registerRestFramework(RestFramework framework) {
        frameworkInstances.put(framework.getIdentifier().toLowerCase(), framework);
    }

    public RestApiParser createParser(String frameworkIdentifier, String projectPath, String outputFileName) {
        RestFramework framework = frameworkInstances.get(frameworkIdentifier.toLowerCase());

        if (framework == null) {
            throw new IllegalArgumentException("Unsupported framework: " + frameworkIdentifier);
        }

        return new RestApiParser(projectPath, outputFileName, framework);
    }

    public RestApiParser createParserWithDetection(String projectPath, String restApiModulePath, String outputFileName) {
        FrameworkDetector frameworkDetector = new FrameworkDetector(frameworkInstances);
        RestFramework detectedFramework = frameworkDetector.detectFramework(projectPath);

        if (detectedFramework == null) {
            throw new IllegalArgumentException("No rest framework could be detected");
        }

        return new RestApiParser(projectPath, restApiModulePath, outputFileName, detectedFramework, frameworkDetector.getModel());
    }

    public RestApiParser createParserWithDetection(String projectPath, String outputFileName) {
        FrameworkDetector frameworkDetector = new FrameworkDetector(frameworkInstances);
        RestFramework detectedFramework = frameworkDetector.detectFramework(projectPath);

        if (detectedFramework == null) {
            throw new IllegalArgumentException("No rest framework could be detected");
        }

        return new RestApiParser(projectPath, outputFileName, detectedFramework, frameworkDetector.getModel());
    }
}
