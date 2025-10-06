package com.github.jrcodeza.schema.generator.interceptors;

import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.NotImplementedException;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.lang.reflect.Field;

public interface SchemaFieldInterceptor {

	void intercept(Class<?> clazz, Field field, Schema<?> transformedFieldSchema);

	default void intercept(CtType<?> clazz, CtField<?> field, Schema<?> transformedFieldSchema){
		throw new NotImplementedException();
	}

	// used for getter methods for schema property generation
	default void intercept(CtType<?> clazz, CtMethod<?> method, Schema<?> transformedFieldSchema){
		throw new NotImplementedException();
	}
}
