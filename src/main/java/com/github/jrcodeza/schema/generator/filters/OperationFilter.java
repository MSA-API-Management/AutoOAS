package com.github.jrcodeza.schema.generator.filters;

import org.apache.commons.lang3.NotImplementedException;
import spoon.reflect.declaration.CtMethod;

import java.lang.reflect.Method;

public interface OperationFilter {
    boolean shouldIgnore(Method method);

//    default boolean shouldIgnore(CtMethod<?> method){
//        throw new NotImplementedException();
//    }
}
