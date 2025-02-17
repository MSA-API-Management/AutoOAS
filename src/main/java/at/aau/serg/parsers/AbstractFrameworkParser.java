package at.aau.serg.parsers;

import spoon.Launcher;
import spoon.MavenLauncher;
import spoon.OutputType;
import spoon.reflect.CtModel;
import spoon.support.compiler.VirtualFolder;

import java.io.File;

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
