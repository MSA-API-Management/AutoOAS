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

    public RestApiParser createParserWithDetection(String projectPath, String restApiModulePath, String outputFileName, boolean exceptionLoggingEnabled) {
        FrameworkDetector frameworkDetector = new FrameworkDetector(frameworkInstances);
        RestFramework detectedFramework = frameworkDetector.detectFramework(restApiModulePath);

        if (detectedFramework == null) {
            throw new IllegalArgumentException("No rest framework could be detected");
        }

        detectedFramework.setExceptionLoggingEnabled(exceptionLoggingEnabled);

        return new RestApiParser(projectPath, restApiModulePath, outputFileName, detectedFramework);
    }
}
