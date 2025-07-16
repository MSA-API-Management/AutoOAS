package at.aau.serg.parsers;

import spoon.Launcher;
import spoon.MavenLauncher;
import spoon.OutputType;
import spoon.reflect.CtModel;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SpoonModelLoader {
    // todo jar arg
    protected boolean deleteSpoonTmpFile = true;

    /**
     * @author Christian
     */
    public CtModel loadModel(String path) {
        System.out.println("Loading model: " + path);

        if (deleteSpoonTmpFile) {
            new File(path + "/spoon.classpath-app.tmp").delete();
        }

        if (isMavenProject(path)) {
            System.out.println("Detected Maven project, using MavenLauncher");
            MavenLauncher launcher = new MavenLauncher(path, MavenLauncher.SOURCE_TYPE.APP_SOURCE);
            launcher.getEnvironment().setComplianceLevel(11);
            launcher.getEnvironment().setOutputType(OutputType.COMPILATION_UNITS);
            launcher.getEnvironment().setNoClasspath(true);
            launcher.buildModel();

            return launcher.getModel();
        } else {
            System.out.println("No Maven project was found, using Standard Launcher");
            Launcher launcher = new Launcher();
            launcher.getEnvironment().setComplianceLevel(11);
            launcher.getEnvironment().setOutputType(OutputType.COMPILATION_UNITS);
            launcher.getEnvironment().setNoClasspath(true);
            launcher.addInputResource(path);
            launcher.buildModel();

            return launcher.getModel();
        }
    }

    private boolean isMavenProject(String path) {
        return Files.exists(Paths.get(path, "pom.xml"));
    }
}
