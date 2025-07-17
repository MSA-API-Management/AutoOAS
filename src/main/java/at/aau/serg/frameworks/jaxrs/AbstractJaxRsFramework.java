package at.aau.serg.frameworks.jaxrs;

import at.aau.serg.frameworks.RestFramework;
import at.aau.serg.parsers.HttpMethod;
import org.springframework.http.HttpStatus;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;

import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

public abstract class AbstractJaxRsFramework implements RestFramework {

    @Override
    public String getIdentifier() {
        return "JAX-RS";
    }

    //  Todo: We do not support profiles for JAX-RS implementations (eg. Quarkus) currently

    @Override
    public Map<String, List<CtType<?>>> splitClassesOnProfiles(List<CtType<?>> controllerClasses) {
        //  Jax-RS does not define any profile functionality
        //  Jersey similarly does not provide profile functionality
        //  Quarkus does, @IfBuildProfile(allOf / anyOf = {"dev","prod"})

        return Map.of("default", controllerClasses);
    }

    @Override
    public String getProfileAnnotation() {
        return "";
    }

    @Override
    public Class<?> getAsyncResultWrapper() {
        return CompletionStage.class;
    }

    @Override
    public Class<? extends Annotation> getRequestBodyAnnotation() {
        return null; // No specific request body annotation exists in JAX-RS
    }

    @Override
    public Optional<Annotation> findRequestMappingAnnotation(CtMethod<?> method) {
        return Optional.empty(); // no generic request mapping exists
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
        return HttpStatus.NO_CONTENT;
    }

    protected boolean isParameterTypeString(CtParameter<?> parameter) {
        String parameterType = parameter.getType().getQualifiedName();
        return parameterType.equals("java.lang.String");
    }
}
