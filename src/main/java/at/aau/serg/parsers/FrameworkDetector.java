package at.aau.serg.parsers;

import at.aau.serg.frameworks.RestFramework;
import lombok.Getter;
import spoon.MavenLauncher;
import spoon.OutputType;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FrameworkDetector {
    private final Map<RestFramework, Set<String>> frameworkAnnotations = new HashMap<>();

    // todo jar arg
    protected boolean deleteSpoonTmpFile = true;

    @Getter
    private CtModel model;

    public FrameworkDetector(Map<String, RestFramework> frameworkInstances) {
        for (RestFramework framework : frameworkInstances.values()) {
            frameworkAnnotations.put(framework, framework.getKeyAnnotations());
        }
    }

    public RestFramework detectFramework(String projectPath) {
        this.model = loadModel(projectPath);

        var packages = model.getAllPackages();

        for (CtPackage pkg : packages) {
            for (CtType<?> type : pkg.getTypes()) {
                RestFramework framework = checkAnnotations(type.getAnnotations());
                if (framework != null) {
                    System.out.println("Detected Framework via Type Annotations: " + framework.getIdentifier());
                    return framework;
                }

                for (var method : type.getMethods()) {
                    framework = checkAnnotations(method.getAnnotations());

                    if (framework != null) {
                        System.out.println("Detected Framework via Method Annotations: " + framework.getIdentifier());
                    }
                }
            }
        }
        System.out.println("No RestFramework detected");
        return null;
    }

    private RestFramework checkAnnotations(List<CtAnnotation<?>> annotations) {
        for (CtAnnotation<?> annotation : annotations) {
            String annotationName = annotation.getAnnotationType().toString();

            if (annotationName != null) {
                for (Map.Entry<RestFramework, Set<String>> entry : frameworkAnnotations.entrySet()) {
                    if (entry.getValue().contains(annotationName)) {
                        return entry.getKey();
                    }
                }
            }
        }
        return null;
    }

    /**
     * @author Christian
     */
    private CtModel loadModel(String path) {
        System.out.println("Loading model: " + path);

        if (deleteSpoonTmpFile) {
            new File(path + "/spoon.classpath-app.tmp").delete();
        }

        MavenLauncher launcher = new MavenLauncher(path, MavenLauncher.SOURCE_TYPE.APP_SOURCE);
        launcher.getEnvironment().setComplianceLevel(11);
        launcher.getEnvironment().setOutputType(OutputType.COMPILATION_UNITS);
        launcher.getEnvironment().setNoClasspath(true);

        launcher.buildModel();
        return launcher.getModel();
    }
}
