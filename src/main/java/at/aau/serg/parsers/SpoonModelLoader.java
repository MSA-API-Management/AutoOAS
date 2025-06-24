package at.aau.serg.parsers;

import spoon.MavenLauncher;
import spoon.OutputType;
import spoon.reflect.CtModel;

import java.io.File;

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

        MavenLauncher launcher = new MavenLauncher(path, MavenLauncher.SOURCE_TYPE.APP_SOURCE);
        launcher.getEnvironment().setComplianceLevel(11);
        launcher.getEnvironment().setOutputType(OutputType.COMPILATION_UNITS);
        launcher.getEnvironment().setNoClasspath(true);

        launcher.buildModel();
        return launcher.getModel();
    }
}
