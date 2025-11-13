package at.aau.serg.parsers;

import spoon.MavenLauncher;
import spoon.OutputType;
import spoon.reflect.CtModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SpoonModelLoader {
    private static final int EXPECTED_JAVA_VERSION = 21;

    // todo jar arg
    private static final String JAVA_11_PATH = System.getenv().getOrDefault("JAVA11_HOME", null);

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

        if (!isMavenProject(path) && isGradleProject(path)) {
            System.out.println("Gradle project detected, trying to generate a pom for dependency resolution.");

            try {
                boolean success = tryGeneratePomFromGradle(path);
                if (!success) {
                    throw new IllegalArgumentException("Project " + path + " is not parsable with dependency resolution.");
                }
            } catch (Exception e) {
                System.out.println("Gradle-to-Maven conversion failed: " + e.getMessage());
                throw new IllegalArgumentException("Project " + path + " is not parsable with dependency resolution.");
            }
        }

        MavenLauncher launcher = new MavenLauncher(path, MavenLauncher.SOURCE_TYPE.APP_SOURCE);
        launcher.getEnvironment().setComplianceLevel(EXPECTED_JAVA_VERSION);
        launcher.getEnvironment().setOutputType(OutputType.COMPILATION_UNITS);
        launcher.getEnvironment().setNoClasspath(true);
        launcher.buildModel();

        return launcher.getModel();
    }

    private boolean isMavenProject(String path) {
        return Files.exists(Paths.get(path, "pom.xml"));
    }

    private boolean isGradleProject(String path) {
        return new File(path, "build.gradle").exists();
        // Gradle Kotlin does not support generating poms ootb anymore ("build.gradle.kts")
    }

    /**
     * Use Gradle's built-in `maven-publish` mechanism to generate a pom
     *
     * @param projectDir
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private boolean tryGeneratePomFromGradle(String projectDir) throws IOException, InterruptedException {
        if (!isGradleProject(projectDir))
            return false;

        // run: ./gradlew -q pom > generated-pom.xml
        ProcessBuilder builder = new ProcessBuilder()
                .directory(new File(projectDir))
                .command(getGradleCommand(projectDir), "pom", "-q");

        if (JAVA_11_PATH != null)
            builder.environment().put("JAVA_HOME", JAVA_11_PATH); // for legacy support
        builder.redirectErrorStream(true);

        Process process = builder.start();
        int exitCode = process.waitFor();

        return exitCode == 0;
    }
    private String getGradleCommand(String projectDir) {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return new File(projectDir, "gradlew.bat").getAbsolutePath();
        } else {
            return new File(projectDir, "gradlew").getAbsolutePath();
        }
    }
}