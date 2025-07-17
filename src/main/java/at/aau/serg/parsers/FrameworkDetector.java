package at.aau.serg.parsers;

import at.aau.serg.frameworks.RestFramework;
import lombok.Getter;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrameworkDetector {
    private final Map<String, RestFramework> annotationToFramework = new HashMap<>();

    @Getter
    private CtModel model;

    public FrameworkDetector(Map<String, RestFramework> frameworkInstances) {
        for (RestFramework framework : frameworkInstances.values()) {
            for (String annotation : framework.getKeyAnnotations()) {
                annotationToFramework.put(annotation, framework);
            }
        }
    }

    public RestFramework detectFramework(String projectPath) {
        this.model = new SpoonModelLoader().loadModel(projectPath);

        for (CtPackage pkg : model.getAllPackages()) {
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
                        return framework;
                    }
                }
            }
        }

        System.out.println("No RestFramework detected");
        return null;
    }

    private RestFramework checkAnnotations(List<CtAnnotation<?>> annotations) {
        if (annotations == null || annotations.isEmpty()) {
            return null;
        }

        for (CtAnnotation<?> annotation : annotations) {
            String annotationName = annotation.getAnnotationType().toString();

            RestFramework framework = annotationToFramework.get(annotationName);
            if (framework != null) {
                return framework;
            }
        }
        return null;
    }
}
