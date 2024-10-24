package com.github.jrcodeza.schema.generator.filters;

import org.apache.commons.lang3.NotImplementedException;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.lang.reflect.Field;

public interface SchemaFieldFilter {
    boolean shouldIgnore(Class<?> clazz, Field field);

    default boolean shouldIgnore(CtType<?> clazz, CtField<?> field) {
        throw new NotImplementedException();
    }
}
