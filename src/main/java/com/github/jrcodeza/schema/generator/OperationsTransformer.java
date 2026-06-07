package com.github.jrcodeza.schema.generator;

import at.aau.serg.annotations.Out;
import at.aau.serg.frameworks.*;
import at.aau.serg.parsers.*;
import com.github.jrcodeza.schema.generator.filters.OperationParameterFilter;
import com.github.jrcodeza.schema.generator.interceptors.OperationInterceptor;
import com.github.jrcodeza.schema.generator.interceptors.OperationParameterInterceptor;
import com.github.jrcodeza.schema.generator.interceptors.RequestBodyInterceptor;
import com.github.jrcodeza.schema.generator.util.SchemaGeneratorHelper;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.jrcodeza.schema.generator.util.GeneratorUtils.shouldBeIgnored;
import static java.util.Collections.singletonList;

public class OperationsTransformer {

	private static final Logger logger = LoggerFactory.getLogger(OperationsTransformer.class);
	private final SchemaGeneratorHelper schemaGeneratorHelper;
	private final DataTypeTransformer dataTypeTransformer;
	private final MethodResponseExtractor methodResponseExtractor;
	private final List<OperationParameterInterceptor> operationParameterInterceptors;
	private final List<OperationInterceptor> operationInterceptors;
	private final List<RequestBodyInterceptor> requestBodyInterceptors;
	private final List<com.github.jrcodeza.schema.generator.model.Header> globalHeaders;
	private final RestFramework restFramework;
	private final AtomicReference<OperationParameterFilter> operationParameterFilter;

	public OperationsTransformer(SchemaGeneratorHelper schemaGeneratorHelper,
								 DataTypeTransformer dataTypeTransformer,
								 MethodResponseExtractor methodResponseExtractor,
								 List<OperationParameterInterceptor> operationParameterInterceptors,
								 List<OperationInterceptor> operationInterceptors,
								 List<RequestBodyInterceptor> requestBodyInterceptors,
								 List<com.github.jrcodeza.schema.generator.model.Header> globalHeaders,
								 AtomicReference<OperationParameterFilter> operationParameterFilter,
								 RestFramework restFramework) {
        this.schemaGeneratorHelper = schemaGeneratorHelper;
        this.dataTypeTransformer = dataTypeTransformer;
		this.methodResponseExtractor = methodResponseExtractor;
        this.operationParameterInterceptors = operationParameterInterceptors;
        this.operationInterceptors = operationInterceptors;
        this.requestBodyInterceptors = requestBodyInterceptors;
        this.globalHeaders = globalHeaders;
        this.operationParameterFilter = operationParameterFilter;
        this.restFramework = restFramework;
    }

	/**
	 * Creates all the operations, e.g., HTTP Get, Post.
	 * @param method
	 * @param baseControllerPath Used as path prefix in OpenAPI spec, e.g., /_customers_/{id}.
	 * @param operationsMap
	 * @param controllerClassName Used as tag in OpenAPI spec.
	 */
	public void createOperation(CtMethod<?> method, String baseControllerPath, @Out Map<String, PathItem> operationsMap, String controllerClassName, DetectedApiParamsAndResponses previouslyDetectedApiParamsAndResponses) {
        logger.debug("Transforming {} controller method", method.getSimpleName());
        restFramework.findPostMappingAnnotation(method).ifPresent(postMapping
                -> mapPost(postMapping, method, operationsMap, controllerClassName, baseControllerPath, previouslyDetectedApiParamsAndResponses));
        restFramework.findPutMappingAnnotation(method).ifPresent(putMapping
                -> mapPut(putMapping, method, operationsMap, controllerClassName, baseControllerPath, previouslyDetectedApiParamsAndResponses));
        restFramework.findPatchMappingAnnotation(method).ifPresent(patchMapping
                -> mapPatch(patchMapping, method, operationsMap, controllerClassName, baseControllerPath, previouslyDetectedApiParamsAndResponses));
        restFramework.findGetMappingAnnotation(method).ifPresent(getMapping
                -> mapGet(getMapping, method, operationsMap, controllerClassName, baseControllerPath, previouslyDetectedApiParamsAndResponses));
        restFramework.findDeleteMappingAnnotation(method).ifPresent(deleteMapping
                -> mapDelete(deleteMapping, method, operationsMap, controllerClassName, baseControllerPath, previouslyDetectedApiParamsAndResponses));
        restFramework.findRequestMappingAnnotation(method).ifPresent(requestMapping
                -> mapRequestMapping(requestMapping, method, operationsMap, controllerClassName, baseControllerPath, previouslyDetectedApiParamsAndResponses));

        // todo handle RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.TRACE
    }

	/**
	 * Handling the @RequestMapping annotation with its http methods.
	 * e.g., @RequestMapping(value = "/get-and-post-method", method = {RequestMethod.GET, RequestMethod.POST})
	 *
	 * @param annotation
	 * @param method
	 * @param operationsMap
	 * @param controllerClassName
	 * @param baseControllerPath
	 * @param previouslyDetectedApiParamsAndResponses
	 */
	private void mapRequestMapping(Annotation annotation, CtMethod<?> method, Map<String, PathItem> operationsMap, String controllerClassName,
								   String baseControllerPath, DetectedApiParamsAndResponses previouslyDetectedApiParamsAndResponses) {
		RestOperationAnnotation restOperationAnnotation = restFramework.convertToRequestAnnotation(annotation, method);
		String path = getFirstFromArray(restOperationAnnotation.path());
		String fullPath = prepareUrl(baseControllerPath, "/", path);
		String cleanedPath = removeRegexFromPath(fullPath);

		logger.debug("Called mapRequestMapping with annotation name \"{}\", path {}, method {}, produces {}", restOperationAnnotation.name(), getFirstFromArray(restOperationAnnotation.path()), Arrays.toString(restOperationAnnotation.method()), getFirstFromArray(restOperationAnnotation.produces()));

		// the RequestMapping annotation allows for an empty http methods field, which accepts all
		HttpMethod[] methods = restOperationAnnotation.method().length == 0
				? restFramework.getAllSupportedHttpMethods()
				: restOperationAnnotation.method();

		// create unique operations with unique id per http method
		for (var httpMethod : methods) {
			Operation operation = new Operation();
			operation.setOperationId(getOperationId(cleanedPath, restOperationAnnotation.name(), method, HttpMethod.valueOf(httpMethod.name())));
			operation.setSummary(!StringUtils.isBlank(restOperationAnnotation.name()) ? restOperationAnnotation.name() : method.getSimpleName());
			operation.setTags(singletonList(classNameToTag(controllerClassName)));

			if (restFramework.isAnyHttpMethodWithRequestBody(httpMethod)) {
				operation.setRequestBody(createRequestBody(method, getFirstFromArray(restOperationAnnotation.consumes())));
			}
			operation.setParameters(transformParameters(fullPath, method));
			operation.setResponses(createApiResponses(method, getFirstFromArray(restOperationAnnotation.produces())));

			operationInterceptors.forEach(interceptor -> interceptor.intercept(method, operation, cleanedPath));

			updateOperationWithPreviouslyDetectedParamsAndResponses(operation, previouslyDetectedApiParamsAndResponses);

			updateOperationsMap(cleanedPath, operationsMap,
					pathItem -> setContentBasedOnHttpMethod(pathItem, httpMethod, operation)
			);
		}
	}

	private String prepareUrl(String... url) {
		String preparedUrl = Stream.of(url).filter(Objects::nonNull).collect(Collectors.joining());

		preparedUrl = preparedUrl.replaceAll("//+", "/");

		// potentially remove trailing /
		if (preparedUrl.charAt(preparedUrl.length() - 1) == '/') {
			preparedUrl = preparedUrl.substring(0, preparedUrl.length() - 1);
		}

		// potentially add starting /
		if (!preparedUrl.startsWith("/")) {
			preparedUrl = "/" + preparedUrl;
		}
		return preparedUrl; // .replaceAll("[^A-Za-z0-9-/{}.]", "");
	}

	/**
	 * Sets the http method for the path item based on the methods field in the @RequestMapping annotation
	 * e.g., @RequestMapping(value = "/get-and-post-method", method = {RequestMethod.GET, RequestMethod.POST})
	 * @param pathItem
	 * @param method
	 * @param operation
	 */
	private void setContentBasedOnHttpMethod(PathItem pathItem, HttpMethod method, Operation operation) {
		switch (method) {
			case GET    -> pathItem.setGet(operation);
			case PUT    -> pathItem.setPut(operation);
			case POST   -> pathItem.setPost(operation);
			case PATCH  -> pathItem.setPatch(operation);
			case HEAD   -> pathItem.setHead(operation);
			case OPTIONS-> pathItem.setOptions(operation);
			case DELETE -> pathItem.setDelete(operation);
			case TRACE  -> pathItem.setTrace(operation);
		}
	}


	private String classNameToTag(String controllerClassName) {
		return Stream.of(StringUtils.splitByCharacterTypeCamelCase(controllerClassName))
				.map(StringUtils::lowerCase)
				.collect(Collectors.joining("-"));
	}

	private void mapDelete(Annotation annotation, CtMethod<?> method, Map<String, PathItem> operationsMap, String controllerClassName,
						   String baseControllerPath, DetectedApiParamsAndResponses previouslyDetectedApiParamsAndResponses) {
		RestOperationAnnotation restOperationAnnotation = restFramework.convertToRequestAnnotation(annotation, method);
		String path = getFirstFromArray(restOperationAnnotation.path());
		String fullPath = prepareUrl(baseControllerPath, "/", path);
		String cleanedPath = removeRegexFromPath(fullPath);

		Operation operation = new Operation();
		operation.setOperationId(getOperationId(cleanedPath, restOperationAnnotation.name(), method, HttpMethod.DELETE));
		operation.setSummary(!StringUtils.isBlank(restOperationAnnotation.name()) ? restOperationAnnotation.name() : method.getSimpleName());
		operation.setTags(singletonList(classNameToTag(controllerClassName)));

		operation.setParameters(transformParameters(fullPath, method));
		operation.setResponses(createApiResponses(method, getFirstFromArray(restOperationAnnotation.produces())));

		operationInterceptors.forEach(interceptor -> interceptor.intercept(method, operation, cleanedPath));

		updateOperationWithPreviouslyDetectedParamsAndResponses(operation, previouslyDetectedApiParamsAndResponses);

		updateOperationsMap(cleanedPath, operationsMap, pathItem -> {
            if (pathItem.getDelete() != null)
                logger.error("Found duplicate DELETE mapping for path: {} /// {}", pathItem.getDelete().getOperationId(), operation.getOperationId());
            pathItem.setDelete(operation);
        });
	}

	public ApiResponses createApiResponses(CtMethod<?> method, String produces) {
		return methodResponseExtractor.createApiResponses(method, produces);
	}

	private void mapGet(Annotation annotation, CtMethod<?> method, Map<String, PathItem> operationsMap, String controllerClassName, String baseControllerPath, DetectedApiParamsAndResponses previouslyDetectedApiParamsAndResponses) {
		RestOperationAnnotation restOperationAnnotation = restFramework.convertToRequestAnnotation(annotation, method);
		logger.debug("Called mapGet with annotation name \"{}\", path {}, method {}, produces {}", restOperationAnnotation.name(), getFirstFromArray(restOperationAnnotation.path()), Arrays.toString(restOperationAnnotation.method()), getFirstFromArray(restOperationAnnotation.produces()));

		String path = getFirstFromArray(restOperationAnnotation.path());
		String fullPath = prepareUrl(baseControllerPath, "/", path);
		String cleanedPath = removeRegexFromPath(fullPath);
		logger.debug("Base Controller Path {}, Path {}, fullPath {}, cleanedPath {}", baseControllerPath, path, fullPath, cleanedPath);

		Operation operation = new Operation();
		operation.setOperationId(getOperationId(cleanedPath, restOperationAnnotation.name(), method, HttpMethod.GET));
		operation.setSummary(!StringUtils.isBlank(restOperationAnnotation.name()) ? restOperationAnnotation.name() : method.getSimpleName());
		operation.setTags(singletonList(classNameToTag(controllerClassName)));

		operation.setParameters(transformParameters(fullPath, method));
		operation.setResponses(createApiResponses(method, getFirstFromArray(restOperationAnnotation.produces())));

		operationInterceptors.forEach(interceptor -> interceptor.intercept(method, operation, cleanedPath));

		updateOperationWithPreviouslyDetectedParamsAndResponses(operation, previouslyDetectedApiParamsAndResponses);

		updateOperationsMap(cleanedPath, operationsMap, pathItem -> {
            if (pathItem.getGet() != null)
                logger.error("Found duplicate GET mapping for path: {} /// {}", pathItem.getGet().getOperationId(), operation.getOperationId());
            pathItem.setGet(operation);
        });
	}

	private void mapPatch(Annotation annotation, CtMethod<?> method, Map<String, PathItem> operationsMap, String controllerClassName, String baseControllerPath, DetectedApiParamsAndResponses previouslyDetectedApiParamsAndResponses) {
		RestOperationAnnotation restOperationAnnotation = restFramework.convertToRequestAnnotation(annotation,method);
		String path = getFirstFromArray(restOperationAnnotation.path());
		String fullPath = prepareUrl(baseControllerPath, "/", path);
		String cleanedPath = removeRegexFromPath(fullPath);

		Operation operation = new Operation();
		operation.setOperationId(getOperationId(cleanedPath, restOperationAnnotation.name(), method, HttpMethod.PATCH));
		operation.setSummary(!StringUtils.isBlank(restOperationAnnotation.name()) ? restOperationAnnotation.name() : method.getSimpleName());
		operation.setTags(singletonList(classNameToTag(controllerClassName)));

		operation.setRequestBody(createRequestBody(method, getFirstFromArray(restOperationAnnotation.consumes())));
		operation.setResponses(createApiResponses(method, getFirstFromArray(restOperationAnnotation.produces())));
		operation.setParameters(transformParameters(fullPath, method));

		operationInterceptors.forEach(interceptor -> interceptor.intercept(method, operation, cleanedPath));

		updateOperationWithPreviouslyDetectedParamsAndResponses(operation, previouslyDetectedApiParamsAndResponses);

		updateOperationsMap(cleanedPath, operationsMap, pathItem -> {
            if (pathItem.getPatch() != null)
                logger.error("Found duplicate PATCH mapping for path: {} /// {}", pathItem.getPatch().getOperationId(), operation.getOperationId());
            pathItem.setPatch(operation);
        });
	}

	private void mapPut(Annotation annotation, CtMethod<?> method, Map<String, PathItem> operationsMap, String controllerClassName, String baseControllerPath, DetectedApiParamsAndResponses previouslyDetectedApiParamsAndResponses) {
		RestOperationAnnotation restOperationAnnotation = restFramework.convertToRequestAnnotation(annotation, method);
		String path = getFirstFromArray(restOperationAnnotation.path());
		String fullPath = prepareUrl(baseControllerPath, "/", path);
		String cleanedPath = removeRegexFromPath(fullPath);

		Operation operation = new Operation();
		operation.setOperationId(getOperationId(cleanedPath, restOperationAnnotation.name(), method, HttpMethod.PUT));
		operation.setSummary(!StringUtils.isBlank(restOperationAnnotation.name()) ? restOperationAnnotation.name() : method.getSimpleName());
		operation.setTags(singletonList(classNameToTag(controllerClassName)));

		operation.setRequestBody(createRequestBody(method, getFirstFromArray(restOperationAnnotation.consumes())));
		operation.setResponses(createApiResponses(method, getFirstFromArray(restOperationAnnotation.produces())));
		operation.setParameters(transformParameters(fullPath, method));

		operationInterceptors.forEach(interceptor -> interceptor.intercept(method, operation, cleanedPath));

		updateOperationWithPreviouslyDetectedParamsAndResponses(operation, previouslyDetectedApiParamsAndResponses);

		updateOperationsMap(cleanedPath, operationsMap, pathItem -> {
            if (pathItem.getPut() != null)
                logger.error("Found duplicate PUT mapping for path: {} /// {}", pathItem.getPut().getOperationId(), operation.getOperationId());
            pathItem.setPut(operation);
        });
	}

	private void mapPost(Annotation annotation, CtMethod<?> method, Map<String, PathItem> operationsMap, String controllerClassName, String baseControllerPath, DetectedApiParamsAndResponses previouslyDetectedApiParamsAndResponses) {
		RestOperationAnnotation restOperationAnnotation = restFramework.convertToRequestAnnotation(annotation, method);
		String path = getFirstFromArray(restOperationAnnotation.path());
		String fullPath = prepareUrl(baseControllerPath, "/", path);
		String cleanedPath = removeRegexFromPath(fullPath);

		Operation operation = new Operation();
		operation.setOperationId(getOperationId(cleanedPath, restOperationAnnotation.name(), method, HttpMethod.POST));
		operation.setSummary(!StringUtils.isBlank(restOperationAnnotation.name()) ? restOperationAnnotation.name() : method.getSimpleName());
		operation.setTags(singletonList(classNameToTag(controllerClassName)));

		operation.setRequestBody(createRequestBody(method, getFirstFromArray(restOperationAnnotation.consumes())));
		operation.setResponses(createApiResponses(method, getFirstFromArray(restOperationAnnotation.produces())));
		operation.setParameters(transformParameters(fullPath, method));

		operationInterceptors.forEach(interceptor -> interceptor.intercept(method, operation, cleanedPath));

		updateOperationWithPreviouslyDetectedParamsAndResponses(operation, previouslyDetectedApiParamsAndResponses);

		updateOperationsMap(cleanedPath, operationsMap, pathItem -> {
            if (pathItem.getPost() != null)
                logger.error("Found duplicate POST mapping for path: {} /// {}", pathItem.getPost().getOperationId(), operation.getOperationId());
            pathItem.setPost(operation);
        });
	}

	private void updateOperationWithPreviouslyDetectedParamsAndResponses(@Out Operation operation,
																		 DetectedApiParamsAndResponses previouslyDetectedApiParamsAndResponses) {
        for (Parameter parameter : previouslyDetectedApiParamsAndResponses.getDetectedApiParameters()) {
            if (operation.getParameters().stream().noneMatch(p -> p.getName().equals(parameter.getName()) && p.getIn().equals(parameter.getIn()))) {
                operation.addParametersItem(parameter);
            }
        }

        operation.getResponses().putAll(previouslyDetectedApiParamsAndResponses.getDetectedApiResponses());
    }

	private void updateOperationsMap(String url, @Out Map<String, PathItem> existingMap, Consumer<PathItem> pathItemUpdater) {
		if (existingMap.containsKey(url)) {
			pathItemUpdater.accept(existingMap.get(url));
		} else {
			PathItem pathItem = new PathItem();
			pathItemUpdater.accept(pathItem);
			existingMap.put(url, pathItem);
		}
	}

	/**
	 * Creates the list of OAS endpoint parameters (not requestbody) for the Java method.
	 * @param endpointPath
	 * @param method
	 * @return
	 */
	public List<io.swagger.v3.oas.models.parameters.Parameter> transformParameters(String endpointPath, CtMethod<?> method) {
		List<CtParameter<?>> parameters = method.getParameters();
		List<io.swagger.v3.oas.models.parameters.Parameter> result = new ArrayList<>();
		addGlobalHeaders(result);

		for (var actualParameter : parameters){
			logger.debug("Transforming Parameters - Actual Param: {} ", actualParameter);
			String parameterName = actualParameter.getSimpleName();

			if (shouldIgnoreParameter(method, actualParameter, parameterName)) {
				logger.info("Ignoring parameter {}", parameterName);
				continue;
			}

			if (isAnnotatedAsOASParameter(actualParameter)) {
				io.swagger.v3.oas.models.parameters.Parameter oasParameter = mapSimpleParameter(actualParameter, parameterName);
				if (oasParameter != null) {

					if ("path".equals(oasParameter.getIn()) && "string".equals(oasParameter.getSchema().getType())){
						// parameter is path variable type string -> check for regex definition in path and add to OAS
						String pattern = tryExtractRegexFromPath(endpointPath, oasParameter.getName());
						if (pattern != null) {
							oasParameter.getSchema().setPattern(pattern);
						}
					}

					operationParameterInterceptors.forEach(interceptor -> interceptor.intercept(method, actualParameter, parameterName, oasParameter));
					result.add(oasParameter);
				}
			}

			// todo no annotation in Spring is the ModelAttribute, ie. parameterGroupAnnotation
			else if (actualParameter.getAnnotation(restFramework.getParameterGroupAnnotation()) != null){
				List<CtField<?>> flattenedFields = flattenParameterFields(actualParameter);
				for (CtField<?> flattenedField : flattenedFields) {
					// currently not supporting translation from annotations!
					io.swagger.v3.oas.models.parameters.Parameter fieldOasParameter = new io.swagger.v3.oas.models.parameters.Parameter();
					fieldOasParameter.setName(flattenedField.getSimpleName());
					fieldOasParameter.setIn("query");

					fieldOasParameter.setSchema(createSchema(flattenedField.getType(), new Annotation[]{}));
					// todo interceptor?
					result.add(fieldOasParameter);
				}
			}

		}
		return result;
	}

	/**
	 * Flattens the parameter type's fields including its inheritance hierarchy
	 * @param parameter
	 * @return
	 */
	private List<CtField<?>> flattenParameterFields(CtParameter<?> parameter) {
		var curType = parameter.getType();

		var fields = new ArrayList<CtField<?>>();

		while (curType != null && curType.getTypeDeclaration() != null){
			var curClass = curType.getTypeDeclaration();
			for (CtField<?> field : curClass.getFields()) {
				// dont consider static fields
				if (field.isStatic())
					continue;

				fields.add(field);
			}

			// iterate inheritance hierarchy
			curType = curType.getSuperclass();
		}

		return fields;
	}

	/**
	 * Extracts the regex from a endpoint path with regex path variable,
	 * e.g., "/regex/{name:^[a-zA-Z0-9]*$}"
	 * @param path
	 * @param parameterName
	 * @return
	 */
	private String tryExtractRegexFromPath(String path, String parameterName) {
		// Define the regex pattern to match the parameter with its regex
		String regexPattern = "\\{" + parameterName + ":(.*?)\\}";

		// Compile the pattern and create a matcher for the given path
		Pattern pattern = Pattern.compile(regexPattern);
		Matcher matcher = pattern.matcher(path);

		// Check if the pattern is found and return the regex group
		if (matcher.find()) {
			return matcher.group(1);
		}

		// Return null if the parameter or regex is not found
		return null;
	}

	private String removeRegexFromPath(String path) {
		// Define the regex pattern to match the parameter with its regex
		String regexPattern = "\\{([^:]+):[^}]*\\}";

		// Use a regular expression to replace parameters with regex by just the parameter name
		Pattern pattern = Pattern.compile(regexPattern);
		Matcher matcher = pattern.matcher(path);

		StringBuffer result = new StringBuffer();
		while (matcher.find()) {
			String parameterName = matcher.group(1);
			matcher.appendReplacement(result, "{" + parameterName + "}");
		}
		matcher.appendTail(result);

		return result.toString();
	}

	private boolean shouldIgnoreParameter(CtMethod<?> method, CtParameter<?> parameter, String parameterName) {
		if (shouldBeIgnored(parameter)) {
			return true;
		}

		return this.operationParameterFilter.get() != null
				&& this.operationParameterFilter.get().shouldIgnore(method, parameter, parameterName);
	}

	private void addGlobalHeaders(List<io.swagger.v3.oas.models.parameters.Parameter> result) {
		if (!globalHeaders.isEmpty()) {
			List<io.swagger.v3.oas.models.parameters.Parameter> globalOasHeaders = globalHeaders.stream()
					.map(this::createOasHeader)
					.collect(Collectors.toList());
			result.addAll(globalOasHeaders);
		}
	}

	private io.swagger.v3.oas.models.parameters.Parameter createOasHeader(com.github.jrcodeza.schema.generator.model.Header header) {
		Schema<?> schema = new Schema<>();
		schema.setType("string");

		io.swagger.v3.oas.models.parameters.Parameter parameter = new io.swagger.v3.oas.models.parameters.Parameter();
		parameter.setIn("header");
		parameter.setName(header.getName());
		parameter.setDescription(header.getDescription());
		parameter.setRequired(header.isRequired());

		parameter.setSchema(schema);
		return parameter;
	}

	/**
	 * Returns true if the parameter is annotated with: PathVariable, RequestParam, or RequestHeader
	 * @param parameter
	 * @return
	 */
	private boolean isAnnotatedAsOASParameter(CtParameter<?> parameter) {
		return restFramework.tryConvertPathVariableAnnotation(parameter) != null ||
				restFramework.tryConvertRequestParamAnnotation(parameter) != null ||
				restFramework.tryConvertRequestHeaderAnnotation(parameter) != null;
	}

	/**
	 * Handles PathVariable (path), RequestParam (query), and RequestHeader (header) parameters.
	 * @param parameter
	 * @param parameterName
	 * @return
	 */
	private io.swagger.v3.oas.models.parameters.Parameter mapSimpleParameter(CtParameter<?> parameter, String parameterName) {
		io.swagger.v3.oas.models.parameters.Parameter oasParameter = new io.swagger.v3.oas.models.parameters.Parameter();
		logger.debug("Mapping simple parameter {} (Parameter: {})", parameterName, parameter);

		PathVariableAnnotation pathVariableAnnotation = restFramework.tryConvertPathVariableAnnotation(parameter);
		if (pathVariableAnnotation != null) {
			oasParameter.setName(resolveNameFromAnnotation(pathVariableAnnotation.value(), parameterName));
			oasParameter.setIn("path");
			oasParameter.setRequired(pathVariableAnnotation.required());
		} else {
			RequestParamAnnotation requestParamAnnotation = restFramework.tryConvertRequestParamAnnotation(parameter);
			if (requestParamAnnotation != null && !parameter.getType().getClass().isAssignableFrom(restFramework.getSupportedFileType())) {
				oasParameter.setName(resolveNameFromAnnotation(requestParamAnnotation.value(), parameterName));
				oasParameter.setIn("query");
				oasParameter.setRequired(requestParamAnnotation.required());
			} else {
				RequestHeaderAnnotation requestHeaderAnnotation = restFramework.tryConvertRequestHeaderAnnotation(parameter);
				if (requestHeaderAnnotation != null) {
					oasParameter.setName(resolveNameFromAnnotation(requestHeaderAnnotation.value(), parameterName));
					oasParameter.setIn("header");
					oasParameter.setRequired(requestHeaderAnnotation.required());
				} else {
					return null;
				}
			}
		}

		oasParameter.setSchema(createSchemaFromParameter(parameter, true));
		schemaGeneratorHelper.enrichWithTypeAnnotations(oasParameter, getActualAnnotations(parameter));
		return oasParameter;
	}

	private String resolveNameFromAnnotation(String valueFromAnnotation, String reflectionParameterName) {
		return Stream.of(valueFromAnnotation, reflectionParameterName)
				.filter(StringUtils::isNotBlank)
				.map(s -> s.replaceAll("/[^A-Za-z0-9]/", ""))
				.findFirst()
				.orElse(null);
	}

	private RequestBody createRequestBody(CtMethod<?> method, String userDefinedContentType) {
		ParameterNamePair requestBodyParameter = getRequestBody(method);

		if (requestBodyParameter == null) {
			return null;
		}

		Content content = new Content();
		AtomicBoolean isOptionalParameter = new AtomicBoolean(false);

		var parameterType = requestBodyParameter.getParameter().getType();
		MediaType mediaType;
		if (parameterType.isPrimitive()) {
			mediaType = new MediaType();
			mediaType.setSchema(schemaGeneratorHelper.parseBaseTypeSignature(parameterType, new Annotation[0]));
		} else {
			mediaType = schemaGeneratorHelper.createMediaType(
					requestBodyParameter.getParameter().getType(),
					requestBodyParameter.getName(),
					isOptionalParameter
			);
		}
		content.addMediaType(dataTypeTransformer.resolveContentType(userDefinedContentType, requestBodyParameter.getParameter()),
				mediaType
		);

		RequestBody requestBody = new RequestBody();
		requestBody.setRequired(!isOptionalParameter.get());
		requestBody.setContent(content);
		requestBody.setDescription("requestBody");

		requestBodyInterceptors.forEach(interceptor ->
				interceptor.intercept(method, requestBodyParameter.getParameter(), requestBodyParameter.getName(), requestBody)
		);

		return requestBody;
	}

	private CtTypeReference<?> getGenericParam(CtTypeReference<?> type) {
		return schemaGeneratorHelper.getGenericParam(type);
	}

	private Schema createSchemaFromParameter(CtParameter<?> parameter, boolean forcePrimitiveSchema) {
		CtTypeReference<?> clazz = parameter.getType();
		Annotation[] annotations = getActualAnnotations(parameter);

		return createSchema(clazz, annotations, forcePrimitiveSchema);
	}

	private Schema createSchema(CtTypeReference<?> parameterClass, Annotation[] annotations) {
		return createSchema(parameterClass, annotations, false);
	}

	private Schema createSchema(CtTypeReference<?> parameterClass, Annotation[] annotations, boolean forcePrimitiveSchema) {
		Schema schema;
		if (parameterClass.isPrimitive()) {
			schema = schemaGeneratorHelper.parseBaseTypeSignature(parameterClass, annotations);
		} else if (parameterClass.isArray()) {
			schema = schemaGeneratorHelper.parseArraySignature(parameterClass.getDeclaringType(), null, annotations);
		} else if (parameterClass.isSubtypeOf(new TypeFactory().get(List.class).getReference())) {
			var listGenericParameter = getGenericParam(parameterClass);
			schema = schemaGeneratorHelper.parseArraySignature(listGenericParameter, null, annotations);
		} else if (parameterClass.isSubtypeOf(new TypeFactory().get(Set.class).getReference())) {
			var listGenericParameter = getGenericParam(parameterClass);
			schema = schemaGeneratorHelper.parseArraySignature(listGenericParameter, null, annotations);
			schema.setUniqueItems(true);
		} else {
			schema = schemaGeneratorHelper.parseClassRefTypeSignature(parameterClass, annotations, null, forcePrimitiveSchema);
		}
		return schema;
	}

	private Annotation[] getActualAnnotations(CtParameter<?> parameter) {
		return schemaGeneratorHelper.getActualAnnotations(parameter.getAnnotations());
	}

	private ParameterNamePair getRequestBody(CtMethod<?> method) {
		List<CtParameter<?>> parameters = method.getParameters();
		CtParameter<?> requestBodyParam = restFramework.findRequestBody(parameters);

		if(requestBodyParam == null) {
			// fall back if nothing was found
			requestBodyParam = parameters.stream()
					.filter(param -> schemaGeneratorHelper.isFile(param.getType()))
					.findFirst()
					.orElse(null);
		}

		return requestBodyParam != null ? new ParameterNamePair(requestBodyParam.getSimpleName(), requestBodyParam) : null;
	}

	private String getOperationId(String path, String nameFromAnnotation, CtMethod<?> method, HttpMethod httpMethod) {
		return StringUtils.isBlank(nameFromAnnotation)
				? method.getSimpleName()
					+ "Using" + httpMethod.name()
					+ "_" + path.replace('/','_').replace("{", "").replace("}", "")
				: nameFromAnnotation;
	}

	public void fixDuplicateOperationIds(Map<String, PathItem> operationsMap) {
		Map<String, Integer> operationIdCount = new HashMap<>();
		operationsMap.values().forEach(pathItem -> {
			handlePotentiallyDuplicatedOperation(pathItem.getHead(), operationIdCount);
			handlePotentiallyDuplicatedOperation(pathItem.getOptions(), operationIdCount);
			handlePotentiallyDuplicatedOperation(pathItem.getPost(), operationIdCount);
			handlePotentiallyDuplicatedOperation(pathItem.getPatch(), operationIdCount);
			handlePotentiallyDuplicatedOperation(pathItem.getPut(), operationIdCount);
			handlePotentiallyDuplicatedOperation(pathItem.getGet(), operationIdCount);
			handlePotentiallyDuplicatedOperation(pathItem.getDelete(), operationIdCount);
		});
	}

	private void handlePotentiallyDuplicatedOperation(Operation operation, Map<String, Integer> operationIdCount) {
		if (operation == null) {
			return;
		}
		String operationId = operation.getOperationId();
		if (operationIdCount.containsKey(operationId)) {
			throw new IllegalStateException("The operationId " + operationId + " is not unique");

//			Integer newValue = operationIdCount.get(operationId) + 1;
//			operation.setOperationId(operationId + "_" + newValue);
//			operationIdCount.put(operationId, newValue);
//			return;
		}
		operationIdCount.put(operationId, 0);
	}

	public String getBaseControllerPath(CtType<?> clazz) {
		return restFramework.findClassRequestMappingAnnotation(clazz)
				.map(requestAnnotation -> getFirstFromArray(requestAnnotation.path()))
				.orElse(""); // ""/"
	}

	public String getFirstFromArray(String[] strings) {
		return strings == null || strings.length == 0 ? null : strings[0];
	}

	static class ParameterNamePair {
		private String name;
		private CtParameter parameter;

		public ParameterNamePair(String name, CtParameter parameter) {
			this.name = name;
			this.parameter = parameter;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public CtParameter getParameter() {
			return parameter;
		}

		public void setParameter(CtParameter parameter) {
			this.parameter = parameter;
		}
	}
}
