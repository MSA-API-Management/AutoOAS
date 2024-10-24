package com.github.jrcodeza.schema.generator.interceptors;

import io.swagger.v3.oas.models.parameters.RequestBody;
import org.apache.commons.lang3.NotImplementedException;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public interface RequestBodyInterceptor {

    void intercept(Method method, Parameter parameter, String parameterName, RequestBody transformedRequestBody);

    default void intercept(CtMethod<?> method, CtParameter<?> parameter, String parameterName, RequestBody transformedRequestBody) {
        throw new NotImplementedException();
    }

}
