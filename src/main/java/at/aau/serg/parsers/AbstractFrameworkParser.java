package at.aau.serg.parsers;

import spoon.Launcher;
import spoon.MavenLauncher;
import spoon.OutputType;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.*;
import spoon.support.compiler.VirtualFolder;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractFrameworkParser implements FrameworkParser {
    protected CtModel model;
    protected String projectName;
    protected String outputFileName;

    // todo jar arg
    protected boolean deleteSpoonTmpFile = true;

    protected AbstractFrameworkParser(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    protected AbstractFrameworkParser(String projectPath, String outputFileName) {
        this(outputFileName);
        this.projectName = projectPath.substring(projectPath.lastIndexOf('/') + 1);
        this.model = loadModel(projectPath);
    }

    // TODO used?
    protected AbstractFrameworkParser(String projectName, VirtualFolder folder, String outputFileName) {
        this(outputFileName);
        this.projectName = projectName;
        this.model = createVirtualModel(folder);
    }

    /**
     * Contains the annotation for Spring profiles.
     *
     * @return A string representing the profile annotation for the framework (e.g., "@Profile" for Spring).
     */
    protected abstract String getProfileAnnotation();

    /**
     * Contains all annotations marking a class as a controller.
     *
     * @return A list of strings representing the annotations for controller classes (e.g., "@RestController", "@Controller").
     */
    protected abstract List<String> getControllerAnnotations();

    /**
     * Contains all annotations marking a class as a controller advice for exception handling.
     *
     * @return A list of strings representing the annotations for controller advice classes (e.g., "@ControllerAdvice").
     */
    protected abstract List<String> getControllerAdviceAnnotations();

    /**
     * Contains all annotations marking a class as a controller advice for exception handling.
     *
     * @return A list of strings representing the annotations for model schema classes (e.g., "@Schema").
     */
    protected abstract List<String> getModelSchemaAnnotations();

    protected Map<String, List<CtType<?>>> splitClassesOnProfiles(List<CtType<?>> controllerClasses) {
        // split the classes based on spring profile annotations
        Map<String, List<CtType<?>>> controllerClassesPerProfile = new HashMap<>();
        List<CtType<?>> controllerClassesInDefaultProfile = new ArrayList<>();

        for (CtType<?> clazz : controllerClasses) {
            boolean profileAnnotationFound = false;

            for (CtAnnotation<? extends Annotation> annotation : clazz.getAnnotations()) {
                if (getProfileAnnotation().equals(annotation.getAnnotationType().toString())) {
                    profileAnnotationFound = true;
                    // add to annotated profiles
                    String[] profiles = (String[]) annotation.getValueAsObject("value");
                    for (String profile : profiles) {
                        controllerClassesPerProfile.putIfAbsent(profile, new ArrayList<>());
                        controllerClassesPerProfile.get(profile).add(clazz);
                    }
                    break;
                }
            }

            if (!profileAnnotationFound) {
                controllerClassesInDefaultProfile.add(clazz);
            }
        }

        // add all classes without profile to each explicit profile
        controllerClassesPerProfile.forEach((k, v) -> v.addAll(controllerClassesInDefaultProfile));

        // also consider the default profile classes alone (e.g., if no profiles exist)
        controllerClassesPerProfile.put("default", controllerClassesInDefaultProfile);

        return controllerClassesPerProfile;
    }

    // TODO getControllerAnnotations, AdviceAnnotations, ModelSchemaAnnotations
    protected RelevantClasses getRelevantClassesFromPackages(Collection<CtPackage> packages) {
        List<CtType<?>> controllerClasses = new LinkedList<>();
        List<CtType<?>> controllerAdviceClasses = new LinkedList<>();
        List<CtType<?>> explicitModelClasses = new LinkedList<>();

        for (CtPackage pkg : packages)
            for (CtType<?> type : pkg.getTypes())
                for (CtAnnotation<?> annotation : type.getAnnotations()) {
                    String annotationName = annotation.getAnnotationType().toString();
                    if (annotationName != null && getControllerAnnotations().contains(annotationName)) {
                        controllerClasses.add(type);
                        break; // annotations
                    }

                    if (annotationName != null && getControllerAdviceAnnotations().contains(annotationName)) {
                        controllerAdviceClasses.add(type);
                        break; // annotations
                    }

                    if (annotationName != null && getModelSchemaAnnotations().contains(annotationName)) {
                        explicitModelClasses.add(type);
                        break; // annotations
                    }
                }

        return new RelevantClasses(controllerClasses, controllerAdviceClasses, explicitModelClasses);
    }

    // TODO used?
    private void getMethodParams(CtMethod<?> method) {
        var params = method.getParameters();
        List<String> paramNames = params.stream().map(CtNamedElement::getSimpleName).collect(Collectors.toList());

        System.out.println(paramNames);
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

    // TODO used?
    private CtModel createVirtualModel(VirtualFolder folder) {
        Launcher launcher = new Launcher();
        launcher.addInputResource(folder);

        launcher.getEnvironment().setNoClasspath(true);
        launcher.buildModel();
        return launcher.getModel();
    }
}
