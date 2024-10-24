package com.github.jrcodeza.schema.generator.interceptors;

import org.apache.commons.lang3.NotImplementedException;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public interface OperationParameterInterceptor {

    void intercept(Method method, Parameter parameter, String parameterName,
                   io.swagger.v3.oas.models.parameters.Parameter transformedParameter);

    default void intercept(CtMethod method, CtParameter parameter, String parameterName,
                           io.swagger.v3.oas.models.parameters.Parameter transformedParameter) {
        throw new NotImplementedException();
    }

}
