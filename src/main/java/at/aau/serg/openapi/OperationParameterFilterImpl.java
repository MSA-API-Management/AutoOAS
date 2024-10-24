package at.aau.serg.openapi;

import com.github.jrcodeza.schema.generator.filters.OperationParameterFilter;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class OperationParameterFilterImpl implements OperationParameterFilter {
    @Override
    public boolean shouldIgnore(Method method, Parameter parameter, String parameterName) {
        return false;
    }

    @Override
    public boolean shouldIgnore(CtMethod<?> method, CtParameter<?> parameter, String parameterName) {
        return false;
    }
}
