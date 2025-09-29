package com.github.jrcodeza.schema.generator.interceptors;

import io.swagger.v3.oas.models.Operation;
import org.apache.commons.lang3.NotImplementedException;
import spoon.reflect.declaration.CtMethod;

import java.lang.reflect.Method;

public interface OperationInterceptor {

    void intercept(Method method, Operation transformedOperation);

    default void intercept(CtMethod<?> method, Operation transformedOperation, String operationPath) {
        throw new NotImplementedException();
    }

}
