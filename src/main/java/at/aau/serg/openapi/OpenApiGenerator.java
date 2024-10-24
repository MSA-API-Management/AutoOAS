package at.aau.serg.openapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OpenApiGenerator {

    public Info getDummyInfo(String title) {
        return getDummyInfo(title, "Test description");
    }

    public Info getDummyInfo(String title, String description) {
        return new Info()
                .title(title)
                .description(description)
                .version("");
    }

    public void writeOpenApiToFile(OpenAPI openApi, String outputFileName) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try {
            Path outputPath = Path.of(outputFileName);
            Files.createDirectories(outputPath.getParent());
            Files.deleteIfExists(outputPath);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputFileName), openApi);
        } catch (IOException e) {
            System.err.println("Could not write openapi file: " + e);
        }
    }

    public OpenAPI createOpenApi(Info info, Paths paths, Components components) {
        // create openapi
        OpenAPI openApi = new OpenAPI();
        openApi.setComponents(components);
        openApi.setPaths(paths);
        openApi.setInfo(info);
        return openApi;
    }

}
