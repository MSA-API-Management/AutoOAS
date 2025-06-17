package at.aau.serg.parsers;

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
    private final Map<String, Set<String>> frameworkAnnotations = new HashMap<>();

    // todo jar arg
    protected boolean deleteSpoonTmpFile = true;

    public FrameworkDetector() {
        frameworkAnnotations.put("spring", Set.of(
                "org.springframework.web.bind.annotation.RestController",
                "org.springframework.web.bind.annotation.RequestMapping"
        ));

        frameworkAnnotations.put("jakarta", Set.of(
                "jakarta.ws.rs.Path",
                "javax.ws.rs.Path"
        ));
    }

    // TODO directly handle specific restframework?
    // TODO give loaded model to restapiparser
    public void addFramework(String identifier, Set<String> keyAnnotations) {
        frameworkAnnotations.put(identifier.toLowerCase(), keyAnnotations);
    }

    public String detectFramework(String projectPath) {
        CtModel model = loadModel(projectPath);

        var packages = model.getAllPackages();

        for (CtPackage pkg : packages) {
            for (CtType<?> type : pkg.getTypes()) {
                String framework = checkAnnotations(type.getAnnotations());
                if (framework != null) {
                    System.out.println("Detected Framework via Annotations: " + framework);
                    return framework;
                }

                for (var method : type.getMethods()) {
                    framework = checkAnnotations(method.getAnnotations());

                    if (framework != null) {
                        System.out.println("Detected Framework via Methods: " + framework);
                    }
                }
            }
        }
        System.out.println("No RestFramework detected");
        return null;
    }

    private String checkAnnotations(List<CtAnnotation<?>> annotations) {
        for (CtAnnotation<?> annotation : annotations) {
            String annotationName = annotation.getAnnotationType().toString();

            if (annotationName != null) {
                for (Map.Entry<String, Set<String>> entry : frameworkAnnotations.entrySet()) {
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
