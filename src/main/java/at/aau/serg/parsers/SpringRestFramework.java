package at.aau.serg.parsers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

public class SpringRestFramework implements RestFramework {

    @Override
    public String getProfileAnnotation() {
        return "org.springframework.context.annotation.Profile";
    }

    @Override
    public List<String> getControllerAnnotations() {
        return Arrays.asList(
                "org.springframework.stereotype.Controller",
                "org.springframework.web.bind.annotation.RestController",
                "org.springframework.data.rest.webmvc.RepositoryRestController"
        );
    }

    @Override
    public List<String> getControllerAdviceAnnotations() {
        return Arrays.asList(
                "org.springframework.web.bind.annotation.ControllerAdvice",
                "org.springframework.web.bind.annotation.RestControllerAdvice"
        );
    }

    @Override
    public List<String> getModelSchemaAnnotations() {
        return Arrays.asList(
                // "io.swagger.v3.oas.annotations.media.Schema"
        );
    }

    @Override
    public Class<?> getResponseWrapper() {
        return ResponseEntity.class;
    }

    @Override
    public Class<?> getFileType() {
        return MultipartFile.class;
    }

    @Override
    public Class<?> getAsyncResultWrapper() {
        return DeferredResult.class;
    }
}
