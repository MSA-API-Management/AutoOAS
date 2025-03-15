package at.aau.serg.parsers;

import at.aau.serg.parsers.spring.SpringRestFramework;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpringParserIntegration {

    private static final String testResourcesPath = "src/test/resources/spring-boot/";

    private RestApiParser parser;

    @ParameterizedTest
    @ValueSource(strings = {
            "22c055",
            "2cc389",
            "06101b",
            "misc" // empty ResponseEntity, <?> generics etc.
    })
    public void integrationTest_OpenApiGeneration_Basics_DefaultStringProfile(String commitId) throws IOException {
        var outputPath = "target/openapi/swagger-" + commitId + ".json";
        parser = new RestApiParser(testResourcesPath + "simple-spring-" + commitId, outputPath, new SpringRestFramework());
        parser.run();

        var outputPathWithProfiles = "target/openapi/swagger-" + commitId + "_default.json";

        assertTrue(FileUtils.contentEquals(
                new File(testResourcesPath + "simple-spring-" + commitId + ".json"),
                new File(outputPathWithProfiles)));
    }

    @Test
    public void integrationTest_OpenApiGeneration_MultipleProfiles() throws IOException {
        var outputPath = "target/openapi/swagger-profiles.json";
        parser = new RestApiParser(testResourcesPath + "spring-profiles-project", outputPath, new SpringRestFramework());
        parser.run();

        // check each profile
        assertTrue(FileUtils.contentEquals(
                new File(testResourcesPath + "spring-profiles_dev.json"),
                new File("target/openapi/swagger-profiles_dev.json")));

        assertTrue(FileUtils.contentEquals(
                new File(testResourcesPath + "spring-profiles_prod.json"),
                new File("target/openapi/swagger-profiles_prod.json")));

        assertTrue(FileUtils.contentEquals(
                new File(testResourcesPath + "spring-profiles_default.json"),
                new File("target/openapi/swagger-profiles_default.json")));
    }


    @ParameterizedTest
    @CsvSource({
            "spring-exceptions-advice-project,spring-exceptions-advice-response-codes.json"
            , "spring-advanced-response-codes,spring-advanced-response-codes.json"
            , "spring-advanced-behaviors,spring-advanced-behaviors.json"
    })
    public void integrationTest_OpenApiGeneration_AdvancedBehaviors_SingleProfile(String projectFolder, String docsPath) throws IOException {
        var genOutputPath = "target/openapi/" + projectFolder + ".json";

        parser = new RestApiParser(testResourcesPath + projectFolder, genOutputPath, new SpringRestFramework());
        parser.run();

        assertTrue(FileUtils.contentEquals(
                new File(testResourcesPath + docsPath),
                new File("target/openapi/" + projectFolder + "_default.json"))
        );
    }

}
