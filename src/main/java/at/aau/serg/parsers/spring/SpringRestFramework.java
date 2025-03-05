package at.aau.serg.parsers.spring;

import at.aau.serg.parsers.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;

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

    // Todo consistency with names and return types (Optional<RequestAnnotation/Annotation)
    @Override
    public Optional<RequestAnnotation> getRequestMapping(CtType<?> clazz) {
        RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
        return requestMapping != null
                ? Optional.of(new SpringRequestMappingAdapter(requestMapping))
                : Optional.empty();
    }

    @Override
    public RequestAnnotation getRequestAnnotation(Annotation annotation) {
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

        return null; // todo just exception and no null checks?
    }

    @Override
    public PathVariableAnnotation getPathVariableAnnotation(CtParameter<?> parameter) {
        PathVariable annotation = parameter.getAnnotation(PathVariable.class);
        return annotation != null ? new SpringPathVariableAdapter(annotation) : null;
    }

    @Override
    public RequestParamAnnotation getRequestParamAnnotation(CtParameter<?> parameter) {
        RequestParam annotation = parameter.getAnnotation(RequestParam.class);
        return annotation != null ? new SpringRequestParamAdapter(annotation) : null;
    }

    @Override
    public RequestHeaderAnnotation getRequestHeaderAnnotation(CtParameter<?> parameter) {
        RequestHeader annotation = parameter.getAnnotation(RequestHeader.class);
        return annotation != null ? new SpringRequestHeaderAdapter(annotation) : null;
    }

    // todo check if action can be just implemented in the adapter and methods are not necessary at all
    @Override
    public String getPathFromAnnotation(Annotation annotation) {
        RequestAnnotation requestAnnotation = getRequestAnnotation(annotation);

        if (requestAnnotation != null) {
            return ObjectUtils.defaultIfNull(getFirstFromArray(requestAnnotation.value()),
                    getFirstFromArray(requestAnnotation.path()));
        }
        return "";
    }

    @Override
    public String getNameFromAnnotation(Annotation annotation) {
        RequestAnnotation requestAnnotation = getRequestAnnotation(annotation);
        return requestAnnotation != null ? requestAnnotation.name() : "";
    }

    @Override
    public String getProducesFromAnnotation(Annotation annotation) {
        RequestAnnotation requestAnnotation = getRequestAnnotation(annotation);
        return requestAnnotation != null ? getFirstFromArray(requestAnnotation.produces()) : "";
    }

    @Override
    public String getConsumesFromAnnotation(Annotation annotation) {
        RequestAnnotation requestAnnotation = getRequestAnnotation(annotation);
        return requestAnnotation != null ? getFirstFromArray(requestAnnotation.consumes()) : "";
    }

    //TODO
    private Optional<Annotation> getAnnotation(CtMethod<?> method, Class<? extends Annotation> annotationClass) {
        return Optional.ofNullable(method.getAnnotation(annotationClass));
    }

    //    TODO
    public String getFirstFromArray(String[] strings) {
        return strings == null || strings.length == 0 ? null : strings[0];
    }
}
