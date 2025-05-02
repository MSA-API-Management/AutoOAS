package at.aau.serg.frameworks.jakarta;

import at.aau.serg.frameworks.*;
import at.aau.serg.frameworks.jakarta.adapters.JakartaPathAdapter;
import at.aau.serg.frameworks.jakarta.adapters.mappings.*;
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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

public class JakartaRestFramework implements RestFramework {


    //TODO
    private Optional<Annotation> getAnnotation(CtMethod<?> method, Class<? extends Annotation> annotationClass) {
        return Optional.ofNullable(method.getAnnotation(annotationClass));
    }

    @Override
    public String getIdentifier() {
        return "";
    }

    @Override
    public OperationInterceptor getOperationResponseCodeInterceptor(List<CtType<?>> adviceClasses) {
        return new JakartaOperationResponseCodeInterceptor(adviceClasses);
    }

    @Override
    public String getProfileAnnotation() {
        return "";
    }

    /**
     * TODO
     * jakarta.ws.rs.Path
     * io.quarkus.vertx.web.RouteBase with RequestScoped, Singleton, ApplicationScoped
     */
    @Override
    public List<String> getControllerAnnotations() {
        return Arrays.asList(
                "jakarta.ws.rs.Path"
        );
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

    /**
     * TODO
     */
    @Override
    public List<String> getModelSchemaAnnotations() {
        return List.of();
    }

    /**
     * TODO
     * jakarta.ws.rs.core.Response.class -> Other implementation
     */
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

    /**
     * TODO
     * java.util.concurrent.CompletionStage.class or java.util.concurrent.CompletableFuture.class
     */
    @Override
    public Class<?> getAsyncResultWrapper() {
        return CompletionStage.class;
    }

    /**
     * jakarta.ws.rs.BeanParam.class
     * TODO check if it will work the same
     */
    @Override
    public Class<? extends Annotation> getParameterGroupAnnotation() {
        return BeanParam.class;
    }

    /**
     * TODO Nothing equivalent exists in Jakarta -> Needs other extraction
     */
    @Override
    public Class<? extends Annotation> getRequestBodyAnnotation() {
        return null;
    }

    // jakarta.ws.rs.POST.class
    @Override
    public Optional<Annotation> getPostMapping(CtMethod<?> method) {
        return getAnnotation(method, POST.class);
    }

    // jakarta.ws.rs.PUT.class
    @Override
    public Optional<Annotation> getPutMapping(CtMethod<?> method) {
        return getAnnotation(method, PUT.class);
    }

    // jakarta.ws.rs.PATCH.class
    @Override
    public Optional<Annotation> getPatchMapping(CtMethod<?> method) {
        return getAnnotation(method, PATCH.class);
    }

    // jakarta.ws.rs.GET.class
    @Override
    public Optional<Annotation> getGetMapping(CtMethod<?> method) {
        return getAnnotation(method, GET.class);
    }

    // jakarta.ws.rs.DELETE.class
    @Override
    public Optional<Annotation> getDeleteMapping(CtMethod<?> method) {
        return getAnnotation(method, DELETE.class);
    }

    //    TODO no requestmapping exists
    @Override
    public Optional<Annotation> getRequestMapping(CtMethod<?> method) {
        return Optional.empty();
    }

    @Override
    public Optional<RequestAnnotation> getRequestMapping(CtType<?> clazz) {
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
}
