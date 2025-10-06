package at.aau.serg.parsers;

import at.aau.serg.frameworks.jaxrs.JakartaRestFramework;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JaxRSParserIntegrationTest {

    private static final String testResourcesPath = "src/test/resources/quarkus/";

    @ParameterizedTest
    @CsvSource({
            "simple-quarkus-22c055,simple-quarkus-22c055.json",
            "simple-quarkus-2cc389,simple-quarkus-2cc389.json",
            "quarkus-responses,quarkus-responses.json"
    })
    public void integrationTest_OpenApiGeneration_Basics(String projectFolder, String docsPath) throws IOException {
        assertOpenApiGeneration(projectFolder, docsPath);
    }

    @ParameterizedTest
    @CsvSource({
            "simple-quarkus-misc,simple-quarkus-misc.json",
            "quarkus-requests-misc,quarkus-requests-misc.json"
    })
    public void integrationTest_OpenApiGeneration_MiscBehavior(String projectFolder, String docsPath) throws IOException {
        assertOpenApiGeneration(projectFolder, docsPath);
    }


    @ParameterizedTest
    @CsvSource({
            "quarkus-behavioral-exception-tests,quarkus-behavioral-exception-tests.json",
            "quarkus-complex-exception-tests,quarkus-complex-exception-tests.json"
    })
    public void integrationTest_OpenApiGeneration_ExceptionalBehavior(String projectFolder, String docsPath) throws IOException {
        assertOpenApiGeneration(projectFolder, docsPath);
    }

    @ParameterizedTest
    @CsvSource({
            "quarkus-async-behavior,quarkus-async-behavior.json"
    })
    public void integrationTest_OpenApiGeneration_AsyncBehavior(String projectFolder, String docsPath) throws IOException {
        assertOpenApiGeneration(projectFolder, docsPath);
    }

    @ParameterizedTest
    @CsvSource({
            "quarkus-resource-chaining,quarkus-resource-chaining.json"
    })
    public void integrationTest_OpenApiGeneration_ResourceChaining(String projectFolder, String docsPath) throws IOException {
        assertOpenApiGeneration(projectFolder, docsPath);
    }

    @ParameterizedTest
    @CsvSource({
            "quarkus-multi-module,rest-api,quarkus-multi-module-rest-api.json",
            "quarkus-multi-module,ignored-rest-api,quarkus-multi-module-ignored-rest-api.json"
    })
    public void integrationTest_OpenApiGeneration_MultiModuleProject_IgnoreApiEndpoints(String projectFolder, String apiModule, String docsPath) throws IOException {
        var genOutputPath = "target/openapi/" + projectFolder + ".json";

        var parser = new RestApiParser(testResourcesPath + projectFolder,
                testResourcesPath + projectFolder + '/' + apiModule,
                genOutputPath,
                new JakartaRestFramework());
        parser.run();

        genOutputPath = "target/openapi/" + projectFolder + "_default.json";
        var moveTargetGetOutputPath = "target/openapi/" + projectFolder + '-' + apiModule + "_default.json";
        Files.move(Paths.get(genOutputPath), Paths.get(moveTargetGetOutputPath), StandardCopyOption.REPLACE_EXISTING);

        assertTrue(FileUtils.contentEquals(
                new File(testResourcesPath + docsPath),
                new File(moveTargetGetOutputPath))
        );
    }


    private void assertOpenApiGeneration(String projectFolder, String docsPath) throws IOException {
        var genOutputPath = "target/openapi/" + projectFolder + ".json";

        var parser = new RestApiParser(testResourcesPath + projectFolder, genOutputPath, new JakartaRestFramework());
        parser.run();

        assertTrue(FileUtils.contentEquals(
                new File(testResourcesPath + docsPath),
                new File("target/openapi/" + projectFolder + "_default.json"))
        );
    }

    // info: not supporting profiles, because Jakarta does not define any profile functionality
    // project: quarkus-profiles-behavior
}
