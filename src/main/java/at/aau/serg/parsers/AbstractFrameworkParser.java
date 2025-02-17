package at.aau.serg.parsers;

import spoon.Launcher;
import spoon.MavenLauncher;
import spoon.OutputType;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.support.compiler.VirtualFolder;

import java.io.File;
import java.util.List;
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
