package at.aau.serg.parsers.spring;

import at.aau.serg.parsers.RestFramework;
import org.apache.commons.lang3.ObjectUtils;
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

    // TODO check if there is a better method to do that
    @Override
    public String getPathFromAnnotation(Annotation annotation) {
        if (annotation instanceof PostMapping) {
            PostMapping mapping = (PostMapping) annotation;
            return ObjectUtils.defaultIfNull(getFirstFromArray(mapping.value()), getFirstFromArray(mapping.path()));
        }
        if (annotation instanceof PutMapping) {
            PutMapping mapping = (PutMapping) annotation;
            return ObjectUtils.defaultIfNull(getFirstFromArray(mapping.value()), getFirstFromArray(mapping.path()));
        }
        if (annotation instanceof PatchMapping) {
            PatchMapping mapping = (PatchMapping) annotation;
            return ObjectUtils.defaultIfNull(getFirstFromArray(mapping.value()), getFirstFromArray(mapping.path()));
        }
        if (annotation instanceof GetMapping) {
            GetMapping mapping = (GetMapping) annotation;
            return ObjectUtils.defaultIfNull(getFirstFromArray(mapping.value()), getFirstFromArray(mapping.path()));
        }
        if (annotation instanceof DeleteMapping) {
            DeleteMapping mapping = (DeleteMapping) annotation;
            return ObjectUtils.defaultIfNull(getFirstFromArray(mapping.value()), getFirstFromArray(mapping.path()));
        }
        if (annotation instanceof RequestMapping) {
            RequestMapping mapping = (RequestMapping) annotation;
            return ObjectUtils.defaultIfNull(getFirstFromArray(mapping.value()), getFirstFromArray(mapping.path()));
        }
        return "";
    }

    @Override
    public String getNameFromAnnotation(Annotation annotation) {
        if (annotation instanceof PostMapping) {
            return ((PostMapping) annotation).name();
        }
        if (annotation instanceof PutMapping) {
            return ((PutMapping) annotation).name();
        }
        if (annotation instanceof DeleteMapping) {
            return ((DeleteMapping) annotation).name();
        }
        if (annotation instanceof GetMapping) {
            return ((GetMapping) annotation).name();
        }
        if (annotation instanceof PatchMapping) {
            return ((PatchMapping) annotation).name();
        }
        if (annotation instanceof RequestMapping) {
            return ((RequestMapping) annotation).name();
        }
        return "";
    }

    @Override
    public String getProducesFromAnnotation(Annotation annotation) {
        if (annotation instanceof PostMapping) {
            return getFirstFromArray(((PostMapping) annotation).produces());
        }
        if (annotation instanceof PutMapping) {
            return getFirstFromArray(((PutMapping) annotation).produces());
        }
        if (annotation instanceof PatchMapping) {
            return getFirstFromArray(((PatchMapping) annotation).produces());
        }
        if (annotation instanceof GetMapping) {
            return getFirstFromArray(((GetMapping) annotation).produces());
        }
        if (annotation instanceof DeleteMapping) {
            return getFirstFromArray(((DeleteMapping) annotation).produces());
        }
        if (annotation instanceof RequestMapping) {
            return getFirstFromArray(((RequestMapping) annotation).produces());
        }
        return "";
    }

    @Override
    public String getConsumesFromAnnotation(Annotation annotation) {
        if (annotation instanceof PostMapping) {
            return getFirstFromArray(((PostMapping) annotation).consumes());
        }
        if (annotation instanceof PutMapping) {
            return getFirstFromArray(((PutMapping) annotation).consumes());
        }
        if (annotation instanceof PatchMapping) {
            return getFirstFromArray(((PatchMapping) annotation).consumes());
        }
        if (annotation instanceof RequestMapping) {
            return getFirstFromArray(((RequestMapping) annotation).consumes());
        }
        return "";
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
