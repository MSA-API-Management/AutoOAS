package at.aau.serg.parsers;

import spoon.support.compiler.VirtualFolder;

import java.util.Arrays;
import java.util.List;

public class SpringParser extends AbstractFrameworkParser {
    public SpringParser(String outputFileName) {
        super(outputFileName);
    }

    public SpringParser(String projectPath, String outputFileName) {
        super(projectPath, outputFileName);
    }

    // TODO used?
    public SpringParser(String projectName, VirtualFolder folder, String outputFileName) {
        super(projectName, folder, outputFileName);
    }

    @Override
    public void run() {
        generateOpenApi(model);
    }

    @Override
    protected List<String> getModelSchemaAnnotations() {
        return Arrays.asList(
//                 "io.swagger.v3.oas.annotations.media.Schema"
        );
    }

    @Override
    protected List<String> getControllerAdviceAnnotations() {
        return Arrays.asList(
                "org.springframework.web.bind.annotation.ControllerAdvice",
                "org.springframework.web.bind.annotation.RestControllerAdvice"
        );
    }

    @Override
    protected List<String> getControllerAnnotations() {
        return Arrays.asList(
                "org.springframework.stereotype.Controller",
                "org.springframework.web.bind.annotation.RestController"
                // todo consider RepositoryRestResource - implicit CRUD endpoints
                , "org.springframework.data.rest.webmvc.RepositoryRestController"
        );
    }

    @Override
    protected String getProfileAnnotation() {
        return "org.springframework.context.annotation.Profile";
    }
}
