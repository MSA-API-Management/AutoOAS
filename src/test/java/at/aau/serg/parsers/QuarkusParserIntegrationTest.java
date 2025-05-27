package at.aau.serg.parsers;

import at.aau.serg.frameworks.jakarta.JakartaRestFramework;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuarkusParserIntegrationTest {

    private static final String testResourcesPath = "src/test/resources/quarkus/";

    private RestApiParser parser;

    @ParameterizedTest
    @CsvSource({
            "simple-quarkus-22c055,simple-quarkus-22c055.json"
            , "simple-quarkus-2cc389,simple-quarkus-2cc389.json"
    })
    public void integrationTest_OpenApiGeneration_Basics(String projectFolder, String docsPath) throws IOException {
        var genOutputPath = "target/openapi/" + projectFolder + ".json";

        parser = new RestApiParser(testResourcesPath + projectFolder, genOutputPath, new JakartaRestFramework());
        parser.run();

        assertTrue(FileUtils.contentEquals(
                new File(testResourcesPath + docsPath),
                new File("target/openapi/" + projectFolder + "_default.json"))
        );
    }

    @ParameterizedTest
    @CsvSource({
            "simple-quarkus-misc,simple-quarkus-misc.json"
            , "quarkus-requests-misc,quarkus-requests-misc.json"
    })
    public void integrationTest_OpenApiGeneration_MiscBehavior(String projectFolder, String docsPath) throws IOException {
        var genOutputPath = "target/openapi/" + projectFolder + ".json";

        parser = new RestApiParser(testResourcesPath + projectFolder, genOutputPath, new JakartaRestFramework());
        parser.run();

        assertTrue(FileUtils.contentEquals(
                new File(testResourcesPath + docsPath),
                new File("target/openapi/" + projectFolder + "_default.json"))
        );
    }

    @ParameterizedTest
    @CsvSource({
            "quarkus-behavioral-exception-tests,quarkus-behavioral-exception-tests.json"
    })
    public void integrationTest_OpenApiGeneration_ExceptionalBehavior(String projectFolder, String docsPath) throws IOException {
        var genOutputPath = "target/openapi/" + projectFolder + ".json";

        parser = new RestApiParser(testResourcesPath + projectFolder, genOutputPath, new JakartaRestFramework());
        parser.run();

        assertTrue(FileUtils.contentEquals(
                new File(testResourcesPath + docsPath),
                new File("target/openapi/" + projectFolder + "_default.json"))
        );
    }

}
