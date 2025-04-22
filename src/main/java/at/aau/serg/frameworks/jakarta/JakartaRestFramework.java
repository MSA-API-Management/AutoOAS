package at.aau.serg.frameworks.jakarta;

import at.aau.serg.frameworks.*;
import at.aau.serg.interceptors.JakartaOperationResponseCodeInterceptor;
import at.aau.serg.parsers.HttpMethod;
import com.github.jrcodeza.schema.generator.interceptors.OperationInterceptor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

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

    @Override
    public List<String> getControllerAnnotations() {
        return List.of();
    }

    @Override
    public List<String> getControllerAdviceAnnotations() {
        return List.of();
    }

    @Override
    public List<String> getModelSchemaAnnotations() {
        return List.of();
    }

    @Override
    public Class<?> getResponseWrapper() {
        return null;
    }

    @Override
    public Class<?> getSupportedFileType() {
        return null;
    }

    @Override
    public Class<?> getAsyncResultWrapper() {
        return null;
    }

    @Override
    public Class<? extends Annotation> getParameterGroupAnnotation() {
        return null;
    }

    @Override
    public Class<? extends Annotation> getRequestBodyAnnotation() {
        return null;
    }

    @Override
    public Optional<Annotation> getPostMapping(CtMethod<?> method) {
        return Optional.empty();
    }

    @Override
    public Optional<Annotation> getPutMapping(CtMethod<?> method) {
        return Optional.empty();
    }

    @Override
    public Optional<Annotation> getPatchMapping(CtMethod<?> method) {
        return Optional.empty();
    }

    @Override
    public Optional<Annotation> getGetMapping(CtMethod<?> method) {
        return Optional.empty();
    }

    @Override
    public Optional<Annotation> getDeleteMapping(CtMethod<?> method) {
        return Optional.empty();
    }

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
        return new HttpMethod[0];
    }

    @Override
    public boolean isAnyHttpMethodWithRequestBody(HttpMethod... methods) {
        return false;
    }
}
