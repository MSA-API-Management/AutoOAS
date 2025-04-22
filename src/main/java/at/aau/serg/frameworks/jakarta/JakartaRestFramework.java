package at.aau.serg.frameworks.jakarta;

import at.aau.serg.frameworks.*;
import at.aau.serg.interceptors.JakartaOperationResponseCodeInterceptor;
import at.aau.serg.parsers.HttpMethod;
import com.github.jrcodeza.schema.generator.interceptors.OperationInterceptor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;

import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
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
     * jakarta.enterprise.context.RequestScoped (need to be checked out)
     */
    @Override
    public List<String> getControllerAnnotations() {
        return List.of();
    }

    /**
     * TODO
     * jakarta.ws.rs.ext.ExceptionMapper
     * jakarta.ws.rs.ext.Provider
     */
    @Override
    public List<String> getControllerAdviceAnnotations() {
        return List.of();
    }

    /**
     * TODO
     *
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
        return null;
    }

    /**
     * TODO
     * jakarta.servlet.http.Part        (need to be checked)
     */
    @Override
    public Class<?> getSupportedFileType() {
        return null;
    }

    /**
     * TODO
     * java.util.concurrent.CompletionStage.class or java.util.concurrent.CompletableFuture.class
     */
    @Override
    public Class<?> getAsyncResultWrapper() {
        return null;
    }

    /**
     * jakarta.ws.rs.BeanParam.class
     * Will not work the same
     */
    @Override
    public Class<? extends Annotation> getParameterGroupAnnotation() {
        return null;
    }

    @Override
    public Class<? extends Annotation> getRequestBodyAnnotation() {
        return null;
    }

    // jakarta.ws.rs.POST.class
    @Override
    public Optional<Annotation> getPostMapping(CtMethod<?> method) {
        return Optional.empty();
    }

    // jakarta.ws.rs.PUT.class
    @Override
    public Optional<Annotation> getPutMapping(CtMethod<?> method) {
        return Optional.empty();
    }

    // jakarta.ws.rs.PATCH.class
    @Override
    public Optional<Annotation> getPatchMapping(CtMethod<?> method) {
        return Optional.empty();
    }

    // jakarta.ws.rs.GET.class
    @Override
    public Optional<Annotation> getGetMapping(CtMethod<?> method) {
        return Optional.empty();
    }

    // jakarta.ws.rs.DELETE.class
    @Override
    public Optional<Annotation> getDeleteMapping(CtMethod<?> method) {
        return Optional.empty();
    }

    //    TODO
    @Override
    public Optional<Annotation> getRequestMapping(CtMethod<?> method) {
        return Optional.empty();
    }

    @Override
    public Optional<RequestAnnotation> getRequestMapping(CtType<?> clazz) {
        return Optional.empty();
    }

    @Override
    public RequestAnnotation convertToRequestAnnotation(Annotation annotation) {
        return null;
    }

    @Override
    public PathVariableAnnotation tryConvertPathVariableAnnotation(CtParameter<?> parameter) {
        return null;
    }

    @Override
    public RequestParamAnnotation tryConvertRequestParamAnnotation(CtParameter<?> parameter) {
        return null;
    }

    @Override
    public RequestHeaderAnnotation tryConvertRequestHeaderAnnotation(CtParameter<?> parameter) {
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
