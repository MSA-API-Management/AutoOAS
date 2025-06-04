package at.aau.serg.parsers;

import at.aau.serg.frameworks.jakarta.JakartaRestFramework;
import at.aau.serg.frameworks.spring.SpringRestFramework;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

// TODO remove after real tests are implemented
public class JakartaParserIntegrationTemp {

    private static final String testResourcesPath = "src/test/resources/quarkus/";

    private RestApiParser parser;

    @ParameterizedTest
    @ValueSource(strings = {
            "simple-quarkus-2cc389",
            "simple-quarkus-22c055"
    })
    public void integrationTest_OpenApiGeneration_Basics_DefaultStringProfile(String projectName) throws IOException {
        var outputPath = "target/openapi/" + projectName + ".json";
        parser = new RestApiParser(testResourcesPath + projectName, outputPath, new JakartaRestFramework());
        parser.run();

        var outputPathWithProfileSuffix = "target/openapi/" + projectName + "_default.json";

        assertTrue(FileUtils.contentEquals(
                new File(testResourcesPath + projectName + "_default.json"),
                new File(outputPathWithProfileSuffix)));
    }

}
