package at.aau.serg.frameworks.spring;

import at.aau.serg.frameworks.*;
import at.aau.serg.frameworks.spring.adapters.mappings.*;
import at.aau.serg.frameworks.spring.adapters.parameters.SpringPathVariableAdapter;
import at.aau.serg.frameworks.spring.adapters.parameters.SpringRequestHeaderAdapter;
import at.aau.serg.frameworks.spring.adapters.parameters.SpringRequestParamAdapter;
import at.aau.serg.interceptors.SpringOperationResponseCodeInterceptor;
import at.aau.serg.parsers.HttpMethod;
import com.github.jrcodeza.schema.generator.DataTypeTransformer;
import com.github.jrcodeza.schema.generator.MethodResponseExtractor;
import com.github.jrcodeza.schema.generator.interceptors.OperationInterceptor;
import com.github.jrcodeza.schema.generator.util.SchemaGeneratorHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Stream;

import static at.aau.serg.frameworks.utils.AnnotationUtils.getAnnotation;

public class SpringRestFramework implements RestFramework {
    private boolean exceptionLoggingEnabled;

    @Override
    public String getIdentifier() {
        return "Spring";
    }

    @Override
    public void setExceptionLoggingEnabled(boolean exceptionLoggingEnabled) {
        this.exceptionLoggingEnabled = exceptionLoggingEnabled;
    }

    @Override
    public OperationInterceptor getOperationResponseCodeInterceptor(List<CtType<?>> globalExceptionHandlerClasses,
                                                                    DataTypeTransformer dataTypeTransformer,
                                                                    SchemaGeneratorHelper schemaHelper,
                                                                    MethodResponseExtractor methodResponseExtractor) {
        return new SpringOperationResponseCodeInterceptor(globalExceptionHandlerClasses);
    }

    @Override
    public Map<String, List<CtType<?>>> splitClassesOnProfiles(List<CtType<?>> controllerClasses) {
        // split the classes based on spring profile annotations
        Map<String, List<CtType<?>>> controllerClassesPerProfile = new HashMap<>();
        List<CtType<?>> controllerClassesInDefaultProfile = new ArrayList<>();

        for (CtType<?> clazz : controllerClasses) {
            boolean profileAnnotationFound = false;

            for (CtAnnotation<? extends Annotation> annotation : clazz.getAnnotations()) {
                if (this.getProfileAnnotation().equals(annotation.getAnnotationType().toString())) {
                    profileAnnotationFound = true;
                    // add to annotated profiles
                    String[] profiles = (String[]) annotation.getValueAsObject("value");
                    for (String profile : profiles) {
                        controllerClassesPerProfile.putIfAbsent(profile, new ArrayList<>());
                        controllerClassesPerProfile.get(profile).add(clazz);
                    }
                    break; // iterating annotations
                }
            }

            if (!profileAnnotationFound) {
                controllerClassesInDefaultProfile.add(clazz);
            }
        }

        // add all classes without profile to each explicit profile
        controllerClassesPerProfile.forEach((k, v) -> v.addAll(controllerClassesInDefaultProfile));

        // also consider the default profile classes alone (e.g., if no profiles exist)
        controllerClassesPerProfile.put("default", controllerClassesInDefaultProfile);

        return controllerClassesPerProfile;
    }

    @Override
    public String getProfileAnnotation() {
        return "org.springframework.context.annotation.Profile";
    }

    @Override
    public List<String> getControllerAnnotations() {
        return List.of(
                "org.springframework.stereotype.Controller",
                "org.springframework.web.bind.annotation.RestController",
                "org.springframework.data.rest.webmvc.RepositoryRestController"
        );
    }

    @Override
    public List<String> getControllerAdviceAnnotations() {
        return List.of(
                "org.springframework.web.bind.annotation.ControllerAdvice",
                "org.springframework.web.bind.annotation.RestControllerAdvice"
        );
    }

    @Override
    public String getApplicationPathAnnotation() {
        return null;
    }

    @Override
    public Set<String> getKeyAnnotations() {
        return Set.of(
                "org.springframework.web.bind.annotation.RestController",
                "org.springframework.web.bind.annotation.RequestMapping"
        );
    }

    @Override
    public boolean isGlobalExceptionHandler(String annotationName, CtType<?> type) {
        return getControllerAdviceAnnotations().contains(annotationName);
    }

    @Override
    public boolean isApplicationPath(String annotationName, CtType<?> type) {
        return false;
    }

    @Override
    public List<String> getModelSchemaAnnotations() {
        return List.of(
                // "io.swagger.v3.oas.annotations.media.Schema"
        );
    }

    @Override
    public Class<?> getResponseWrapper() {
        return ResponseEntity.class;
    }

    @Override
    public Class<?> getSupportedFileType() {
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
    public boolean supportsSubResourceResolution() {
        return false;
    }

    @Override
    public List<SubResource> getSubResourcesInController(CtType<?> controllerType) {
        return List.of();
    }

    @Override
    public CtParameter<?> findRequestBody(List<CtParameter<?>> parameters) {
        return parameters.stream()
                .filter(param -> param.getAnnotation(getRequestBodyAnnotation()) != null)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<CtParameter<?>> findFormFields(List<CtParameter<?>> parameters) {
        return List.of();
    }

    @Override
    public String getFormFieldName(CtParameter<?> parameter) {
        return "";
    }

    @Override
    public Optional<? extends Annotation> findPostMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, PostMapping.class);
    }

    @Override
    public Optional<? extends Annotation> findPutMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, PutMapping.class);
    }

    @Override
    public Optional<? extends Annotation> findPatchMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, PatchMapping.class);
    }

    @Override
    public Optional<? extends Annotation> findGetMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, GetMapping.class);
    }

    @Override
    public Optional<? extends Annotation> findDeleteMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, DeleteMapping.class);
    }

    @Override
    public Optional<? extends Annotation> findRequestMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, RequestMapping.class);
    }

    @Override
    public Optional<RestOperationAnnotation> findClassRequestMappingAnnotation(CtType<?> clazz) {
        RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
        return requestMapping != null
                ? Optional.of(new SpringRequestMappingAdapter(requestMapping))
                : Optional.empty();
    }

    @Override
    public RestOperationAnnotation convertToRequestAnnotation(Annotation annotation, CtMethod<?> method) {
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
    public boolean isAnyHttpMethodWithRequestBody(HttpMethod... methods) {
        return Stream.of(methods).anyMatch(method -> EnumSet.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH).contains(method));
    }

    @Override
    public HttpStatus getVoidMethodStatusCode() {
        return HttpStatus.OK;
    }
}
