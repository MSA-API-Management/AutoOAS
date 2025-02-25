package at.aau.serg.parsers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;
import spoon.reflect.declaration.CtMethod;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    @Override
    public Optional<Annotation> getPostMapping(CtMethod<?> method) {
        return getAnnotation(method, PostMapping.class);
    }

    @Override
    public Optional<Annotation> getPutMapping(CtMethod<?> method) {
        return getAnnotation(method, PutMapping.class);
    }

    @Override
    public Optional<Annotation> getPatchMapping(CtMethod<?> method) {
        return getAnnotation(method, PatchMapping.class);
    }

    @Override
    public Optional<Annotation> getGetMapping(CtMethod<?> method) {
        return getAnnotation(method, GetMapping.class);
    }

    @Override
    public Optional<Annotation> getDeleteMapping(CtMethod<?> method) {
        return getAnnotation(method, DeleteMapping.class);
    }

    @Override
    public Optional<Annotation> getRequestMapping(CtMethod<?> method) {
        return getAnnotation(method, RequestMapping.class);
    }

    //TODO
    private Optional<Annotation> getAnnotation(CtMethod<?> method, Class<? extends Annotation> annotationClass) {
        return Optional.ofNullable(method.getAnnotation(annotationClass));
    }
}
