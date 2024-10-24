package com.github.jrcodeza.schema.generator.filters;

import org.apache.commons.lang3.NotImplementedException;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public interface OperationParameterFilter {
    boolean shouldIgnore(Method method, Parameter parameter, String parameterName);

    default boolean shouldIgnore(CtMethod<?> method, CtParameter<?> parameter, String parameterName) {
        throw new NotImplementedException();
    }
}
