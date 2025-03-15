package at.aau.serg.parsers.spring;

import at.aau.serg.interfaces.*;
import at.aau.serg.parsers.HttpMethod;
import at.aau.serg.parsers.spring.adapter.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
    public Class<?> getSupportedFileTypes() {
        return MultipartFile.class;
    }

    @Override
    public Class<?> getAsyncResultWrapper() {
        return DeferredResult.class;
    }

    @Override
    public Class<? extends Annotation> getParameterGroupAnnotation() {
        return ModelAttribute.class;
    }

    @Override
    public Class<? extends Annotation> getRequestBodyAnnotation() {
        return RequestBody.class;
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

    @Override
    public Optional<RequestAnnotation> getRequestMapping(CtType<?> clazz) {
        RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
        return requestMapping != null
                ? Optional.of(new SpringRequestMappingAdapter(requestMapping))
                : Optional.empty();
    }

    @Override
    public RequestAnnotation convertToRequestAnnotation(Annotation annotation) {
        if (annotation instanceof PostMapping) {
            return new SpringPostMappingAdapter((PostMapping) annotation);
        }
        if (annotation instanceof PutMapping) {
            return new SpringPutMappingAdapter((PutMapping) annotation);
        }
        if (annotation instanceof PatchMapping) {
            return new SpringPatchMappingAdapter((PatchMapping) annotation);
        }
        if (annotation instanceof GetMapping) {
            return new SpringGetMappingAdapter((GetMapping) annotation);
        }
        if (annotation instanceof DeleteMapping) {
            return new SpringDeleteMappingAdapter((DeleteMapping) annotation);
        }
        if (annotation instanceof RequestMapping) {
            return new SpringRequestMappingAdapter((RequestMapping) annotation);
        }

        throw new IllegalArgumentException("No supported annotation found");
    }

    @Override
    public PathVariableAnnotation tryConvertPathVariableAnnotation(CtParameter<?> parameter) {
        PathVariable annotation = parameter.getAnnotation(PathVariable.class);
        return annotation != null ? new SpringPathVariableAdapter(annotation) : null;
    }

    @Override
    public RequestParamAnnotation tryConvertRequestParamAnnotation(CtParameter<?> parameter) {
        RequestParam annotation = parameter.getAnnotation(RequestParam.class);
        return annotation != null ? new SpringRequestParamAdapter(annotation) : null;
    }

    @Override
    public RequestHeaderAnnotation tryConvertRequestHeaderAnnotation(CtParameter<?> parameter) {
        RequestHeader annotation = parameter.getAnnotation(RequestHeader.class);
        return annotation != null ? new SpringRequestHeaderAdapter(annotation) : null;
    }

    @Override
    public HttpMethod[] getAllSupportedHttpMethods() {
        return HttpMethod.values();
    }

    @Override
    public boolean isHttpMethodWithRequestBody(HttpMethod... methods) {
        return Stream.of(methods).anyMatch(method -> EnumSet.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH).contains(method));
    }

    //TODO
    private Optional<Annotation> getAnnotation(CtMethod<?> method, Class<? extends Annotation> annotationClass) {
        return Optional.ofNullable(method.getAnnotation(annotationClass));
    }
}
