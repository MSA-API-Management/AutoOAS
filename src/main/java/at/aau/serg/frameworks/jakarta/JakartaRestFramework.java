package at.aau.serg.frameworks.jakarta;

import at.aau.serg.frameworks.*;
import at.aau.serg.frameworks.jakarta.adapters.JakartaPathAdapter;
import at.aau.serg.frameworks.jakarta.adapters.mappings.JakartaHttpMethodAdapter;
import at.aau.serg.frameworks.jakarta.adapters.parameters.JakartaHeaderParamAdapter;
import at.aau.serg.frameworks.jakarta.adapters.parameters.JakartaPathParamAdapter;
import at.aau.serg.frameworks.jakarta.adapters.parameters.JakartaQueryParamAdapter;
import at.aau.serg.interceptors.JakartaOperationResponseCodeInterceptor;
import at.aau.serg.parsers.HttpMethod;
import com.github.jrcodeza.schema.generator.interceptors.OperationInterceptor;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.springframework.web.bind.annotation.RequestMapping;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

import static at.aau.serg.frameworks.utils.AnnotationUtils.getAnnotation;

public class JakartaRestFramework implements RestFramework {
    @Override
    public String getIdentifier() {
        return "";
    }

    @Override
    public String getOpenApiInfoDescription(String profileName, String projectName) {
        return "Quarkus Profile: " + profileName;
    }

    @Override
    public OperationInterceptor getOperationResponseCodeInterceptor(List<CtType<?>> adviceClasses) {
        return new JakartaOperationResponseCodeInterceptor(adviceClasses);
    }

    @Override
    public String getProfileAnnotation() {
        return "";
    }

    @Override
    public List<String> getControllerAnnotations() {
        return List.of("jakarta.ws.rs.Path");
    }

    /**
     * TODO
     * jakarta.ws.rs.ext.Provider
     * jakarta.ws.rs.ext.ExceptionMapper (no annotation -> interface implementation)
     */
    @Override
    public List<String> getControllerAdviceAnnotations() {
        return Arrays.asList("jakarta.ws.rs.ext.Provider");
    }

    @Override
    public List<String> getModelSchemaAnnotations() {
        return List.of();
    }

    @Override
    public Class<?> getResponseWrapper() {
        return Response.class;
    }

    /**
     * TODO
     * jakarta.ws.rs.core.MultivaluedMap.class;
     * org.jboss.resteasy.reactive.multipart.FileUpload.class; (quarkus)
     * TODO Check which is actually used
     */
    @Override
    public Class<?> getSupportedFileType() {
        return MultivaluedMap.class;
    }

    @Override
    public Class<?> getAsyncResultWrapper() {
        return CompletionStage.class;
    }

    @Override
    public Class<? extends Annotation> getParameterGroupAnnotation() {
        return BeanParam.class;
    }

    @Override
    public Class<? extends Annotation> getRequestBodyAnnotation() {
        return null; // No specific request body annotation exists in quarkus
    }

    @Override
    public Optional<Annotation> findPostMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, POST.class);
    }

    @Override
    public Optional<Annotation> findPutMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, PUT.class);
    }

    @Override
    public Optional<Annotation> findPatchMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, PATCH.class);
    }

    @Override
    public Optional<Annotation> findGetMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, GET.class);
    }

    @Override
    public Optional<Annotation> findDeleteMappingAnnotation(CtMethod<?> method) {
        return getAnnotation(method, DELETE.class);
    }

    @Override
    public Optional<Annotation> findRequestMappingAnnotation(CtMethod<?> method) {
        return Optional.empty();    // no request mapping exists
    }

    @Override
    public Optional<RequestAnnotation> findClassRequestMappingAnnotation(CtType<?> clazz) {
        for (CtAnnotation<?> annotation : clazz.getAnnotations()) {
            String annotationTypeName = annotation.getAnnotationType().getQualifiedName();
            if (annotationTypeName.equals(Path.class.getName())) {
                return Optional.of(new JakartaPathAdapter(annotation, clazz));
            }
        }
        return Optional.empty();
    }

    @Override
    public RequestAnnotation convertToRequestAnnotation(Annotation annotation, CtMethod<?> method) {
        if (annotation instanceof POST) {
            return new JakartaHttpMethodAdapter(method, HttpMethod.POST);
        }
        if (annotation instanceof PUT) {
            return new JakartaHttpMethodAdapter(method, HttpMethod.PUT);
        }
        if (annotation instanceof PATCH) {
            return new JakartaHttpMethodAdapter(method, HttpMethod.PATCH);
        }
        if (annotation instanceof GET) {
            return new JakartaHttpMethodAdapter(method, HttpMethod.GET);
        }
        if (annotation instanceof DELETE) {
            return new JakartaHttpMethodAdapter(method, HttpMethod.DELETE);
        }
        if (annotation instanceof RequestMapping) {
            return null;
        }

        throw new IllegalArgumentException("No supported annotation found");
    }

    @Override
    public PathVariableAnnotation tryConvertPathVariableAnnotation(CtParameter<?> parameter) {
        PathParam annotation = parameter.getAnnotation(PathParam.class);
        if (annotation != null) {
            return new JakartaPathParamAdapter(annotation);
        }
        return null;
    }

    @Override
    public RequestParamAnnotation tryConvertRequestParamAnnotation(CtParameter<?> parameter) {
        QueryParam annotation = parameter.getAnnotation(QueryParam.class);
        if (annotation != null) {
            return new JakartaQueryParamAdapter(annotation);
        }
        return null;
    }

    @Override
    public RequestHeaderAnnotation tryConvertRequestHeaderAnnotation(CtParameter<?> parameter) {
        HeaderParam annotation = parameter.getAnnotation(HeaderParam.class);
        if (annotation != null) {
            return new JakartaHeaderParamAdapter(annotation);
        }
        return null;
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
    public boolean isRestFrameworkInjectedType(CtTypeReference<?> type) {
        String typeName = type.getQualifiedName();
        return typeName.equals("javax.ws.rs.core.SecurityContext") ||
                typeName.equals("javax.ws.rs.core.UriInfo") ||
                typeName.equals("javax.ws.rs.core.HttpHeaders") ||
                typeName.equals("jakarta.ws.rs.core.SecurityContext") ||
                typeName.equals("jakarta.ws.rs.core.UriInfo") ||
                typeName.equals("jakarta.ws.rs.core.HttpHeaders");
    }

    @Override
    public boolean hasRestParameterBindingAnnotation(CtParameter<?> parameter) {
        return parameter.getAnnotations().stream()
                .map(CtAnnotation::getAnnotationType)
                .map(CtTypeReference::getQualifiedName)
                .anyMatch(this::isRestParameterBindingAnnotation);
    }

    private boolean isRestParameterBindingAnnotation(String annotationName) {
        if (annotationName.equals("javax.ws.rs.PathParam") ||
                annotationName.equals("javax.ws.rs.QueryParam") ||
                annotationName.equals("javax.ws.rs.HeaderParam") ||
                annotationName.equals("javax.ws.rs.CookieParam") ||
                annotationName.equals("javax.ws.rs.MatrixParam") ||
                annotationName.equals("javax.ws.rs.BeanParam") ||
//                annotationName.equals("javax.ws.rs.FormParam") ||
                annotationName.equals("javax.ws.rs.core.Context")) {
            return true;
        }

        return annotationName.equals("jakarta.ws.rs.PathParam") ||
                annotationName.equals("jakarta.ws.rs.QueryParam") ||
                annotationName.equals("jakarta.ws.rs.HeaderParam") ||
                annotationName.equals("jakarta.ws.rs.CookieParam") ||
                annotationName.equals("jakarta.ws.rs.MatrixParam") ||
                annotationName.equals("jakarta.ws.rs.core.Context") ||
//                annotationName.equals("jakarta.ws.rs.FormParam") ||
                annotationName.equals("jakarta.ws.rs.BeanParam");
    }
}