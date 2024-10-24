package com.github.jrcodeza.schema.generator.config;

import com.github.jrcodeza.schema.generator.interceptors.examples.OpenApiExampleResolver;
import org.springframework.core.env.Environment;

public class OpenApiGeneratorConfig {

	private boolean generateExamples;

	private OpenApiExampleResolver openApiExampleResolver;

	private Environment environment;

	public boolean isGenerateExamples() {
		return generateExamples;
	}

	public void setGenerateExamples(boolean generateExamples) {
		this.generateExamples = generateExamples;
	}

	public OpenApiExampleResolver getOpenApiExampleResolver() {
		return openApiExampleResolver;
	}

	public void setOpenApiExampleResolver(OpenApiExampleResolver openApiExampleResolver) {
		this.openApiExampleResolver = openApiExampleResolver;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
}
