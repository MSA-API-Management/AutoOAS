package com.github.jrcodeza.schema.generator.util;

import spoon.reflect.declaration.CtElement;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class GeneratorUtils {

	private GeneratorUtils() {
		throw new AssertionError();
	}

	public static boolean shouldBeIgnored(AnnotatedElement annotatedElement) {
		return false; // annotatedElement.getAnnotation(OpenApiIgnore.class) != null;
	}

	public static boolean shouldBeIgnored(Field field) {
		return Modifier.isStatic(field.getModifiers()) || shouldBeIgnored((AnnotatedElement) field);
	}

	public static boolean shouldBeIgnored(CtElement annotatedElement) {
		return false; // annotatedElement.getAnnotation(OpenApiIgnore.class) != null;
	}
}
