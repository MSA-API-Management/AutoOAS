package com.github.jrcodeza.schema.generator;

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
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.jrcodeza.schema.generator.util.GeneratorUtils.shouldBeIgnored;
import static java.util.Collections.singletonList;

public class OperationsTransformer {

	private static final HttpStatus DEFAULT_RESPONSE_STATUS = HttpStatus.OK;
	public static final String UNSPECIFIED_SIMPLE_NAME = "UNSPECIFIED_TYPE";

	private static Logger logger = LoggerFactory.getLogger(OperationsTransformer.class);

	private static final String DEFAULT_CONTENT_TYPE = "application/json";
	private static final String DEFAULT_FILE_RETURN_CONTENT_TYPE = "application/octet-stream";
	private static final String MULTIPART_FORM_DATA_CONTENT_TYPE = "multipart/form-data";

	private final SchemaGeneratorHelper schemaGeneratorHelper;
	private final List<OperationParameterInterceptor> operationParameterInterceptors;
	private final List<OperationInterceptor> operationInterceptors;
	private final List<RequestBodyInterceptor> requestBodyInterceptors;
	private final List<com.github.jrcodeza.schema.generator.model.Header> globalHeaders;
	private final RestFramework restFramework;
	private final AtomicReference<OperationParameterFilter> operationParameterFilter;

	public OperationsTransformer(SchemaGeneratorHelper schemaGeneratorHelper,
								 List<OperationParameterInterceptor> operationParameterInterceptors,
								 List<OperationInterceptor> operationInterceptors,
								 List<RequestBodyInterceptor> requestBodyInterceptors,
								 List<com.github.jrcodeza.schema.generator.model.Header> globalHeaders,
								 AtomicReference<OperationParameterFilter> operationParameterFilter,
								 RestFramework restFramework) {
		this.schemaGeneratorHelper = schemaGeneratorHelper;
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
	public void createOperation(CtMethod<?> method, String baseControllerPath, Map<String, PathItem> operationsMap, String controllerClassName) {
		logger.info("Transforming {} controller method", method.getSimpleName());
		restFramework.findPostMappingAnnotation(method).ifPresent(postMapping -> mapPost(postMapping, method, operationsMap, controllerClassName, baseControllerPath));
		restFramework.findPutMappingAnnotation(method).ifPresent(putMapping -> mapPut(putMapping, method, operationsMap, controllerClassName, baseControllerPath));
		restFramework.findPatchMappingAnnotation(method).ifPresent(patchMapping -> mapPatch(patchMapping, method, operationsMap, controllerClassName,
				baseControllerPath));
		restFramework.findGetMappingAnnotation(method).ifPresent(getMapping -> mapGet(getMapping, method, operationsMap, controllerClassName, baseControllerPath));
		restFramework.findDeleteMappingAnnotation(method).ifPresent(deleteMapping -> mapDelete(deleteMapping, method, operationsMap, controllerClassName,
				baseControllerPath));
		restFramework.findRequestMappingAnnotation(method).ifPresent(requestMapping -> mapRequestMapping(requestMapping, method, operationsMap, controllerClassName,
				baseControllerPath));

		// todo handle RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.TRACE
	}

	/**
	 * Handling the @RequestMapping annotation with its http methods.
	 * e.g., @RequestMapping(value = "/get-and-post-method", method = {RequestMethod.GET, RequestMethod.POST})
	 * @param annotation
	 * @param method
	 * @param operationsMap
	 * @param controllerClassName
	 * @param baseControllerPath
	 */
	private void mapRequestMapping(Annotation annotation, CtMethod<?> method, Map<String, PathItem> operationsMap, String controllerClassName,
								   String baseControllerPath) {
		RequestAnnotation requestAnnotation = restFramework.convertToRequestAnnotation(annotation, method);
		String path = ObjectUtils.defaultIfNull(getFirstFromArray(requestAnnotation.value()),
				getFirstFromArray(requestAnnotation.path()));
		String fullPath = prepareUrl(baseControllerPath, "/", path);
		String cleanedPath = removeRegexFromPath(fullPath);

		// the RequestMapping annotation allows for an empty http methods field, which accepts all
		HttpMethod[] methods = requestAnnotation.method().length == 0
				? restFramework.getAllSupportedHttpMethods()
				: requestAnnotation.method();

		// create unique operations with unique id per http method
		for (var httpMethod : methods) {
			Operation operation = new Operation();
			operation.setOperationId(getOperationId(cleanedPath, requestAnnotation.name(), method, HttpMethod.valueOf(httpMethod.name())));
			operation.setSummary(!StringUtils.isBlank(requestAnnotation.name()) ? requestAnnotation.name() : method.getSimpleName());
			operation.setTags(singletonList(classNameToTag(controllerClassName)));

			if (restFramework.isAnyHttpMethodWithRequestBody(httpMethod)) {
				operation.setRequestBody(createRequestBody(method, getFirstFromArray(requestAnnotation.consumes())));
			}
			operation.setParameters(transformParameters(fullPath, method));
			operation.setResponses(createApiResponses(method, getFirstFromArray(requestAnnotation.produces())));

			operationInterceptors.forEach(interceptor -> interceptor.intercept(method, operation));

			updateOperationsMap(cleanedPath, operationsMap,
					pathItem -> setContentBasedOnHttpMethod(pathItem, httpMethod, operation)
			);
		}
	}

	private String prepareUrl(String... url) {
		String preparedUrl = Stream.of(url).filter(Objects::nonNull).collect(Collectors.joining());
		if (preparedUrl.charAt(preparedUrl.length() - 1) == '/') {
			preparedUrl = preparedUrl.substring(0, preparedUrl.length() - 1);
		}
		preparedUrl = preparedUrl.replaceAll("//", "/");
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
		// TODO enhanced switch
		switch (method) {
			case GET:
				pathItem.setGet(operation);
				return;
			case PUT:
				pathItem.setPut(operation);
				return;
			case POST:
				pathItem.setPost(operation);
				return;
			case PATCH:
				pathItem.setPatch(operation);
				return;
			case HEAD:
				pathItem.setHead(operation);
				return;
			case OPTIONS:
				pathItem.setOptions(operation);
				return;
			case DELETE:
				pathItem.setDelete(operation);
				return;
			case TRACE:
				pathItem.setTrace(operation);
		}
	}

	private String classNameToTag(String controllerClassName) {
		return Stream.of(StringUtils.splitByCharacterTypeCamelCase(controllerClassName))
				.map(StringUtils::lowerCase)
				.collect(Collectors.joining("-"));
	}

	private void mapDelete(Annotation annotation, CtMethod<?> method, Map<String, PathItem> operationsMap, String controllerClassName,
						   String baseControllerPath) {
		RequestAnnotation requestAnnotation = restFramework.convertToRequestAnnotation(annotation, method);
		String path = ObjectUtils.defaultIfNull(getFirstFromArray(requestAnnotation.value()),
				getFirstFromArray(requestAnnotation.path()));
		String fullPath = prepareUrl(baseControllerPath, "/", path);
		String cleanedPath = removeRegexFromPath(fullPath);

		Operation operation = new Operation();
		operation.setOperationId(getOperationId(cleanedPath, requestAnnotation.name(), method, HttpMethod.DELETE));
		operation.setSummary(!StringUtils.isBlank(requestAnnotation.name()) ? requestAnnotation.name() : method.getSimpleName());
		operation.setTags(singletonList(classNameToTag(controllerClassName)));

		operation.setParameters(transformParameters(fullPath, method));
		operation.setResponses(createApiResponses(method, getFirstFromArray(requestAnnotation.produces())));

		operationInterceptors.forEach(interceptor -> interceptor.intercept(method, operation));
		updateOperationsMap(cleanedPath, operationsMap, pathItem -> pathItem.setDelete(operation));
	}

	private ApiResponses createApiResponses(CtMethod<?> method, String produces) {
		// todo merge logic for DeferredResult, ResponseEntity stripping

		CtTypeReference<?> methodReturnType = method.getType();
		// strip the DeferredResult wrapper
		if (schemaGeneratorHelper.isTypeEquivalent(methodReturnType, restFramework.getAsyncResultWrapper())){
			methodReturnType = stripReturnValueWrapper(methodReturnType);
		}

		// strip the ResponseEntity wrapper
		if (schemaGeneratorHelper.isTypeEquivalent(methodReturnType, restFramework.getResponseWrapper())){
			methodReturnType = stripReturnValueWrapper(methodReturnType);
		}

		ApiResponse apiResponse = new ApiResponse();

		if (methodReturnType.getSimpleName().equals("void")) {
			// dont add a content
		} else if (methodReturnType.getPackage() != null && methodReturnType.getPackage().getSimpleName().equals("java.lang")) {
			Content content = new Content();
			MediaType simpleMediaType = new MediaType();
			simpleMediaType.setSchema(schemaGeneratorHelper.parseClassRefTypeSignature(methodReturnType, null, null));
			content.addMediaType(StringUtils.isBlank(produces) ? resolveDefaultContentType(methodReturnType) : produces, simpleMediaType);
			apiResponse.setContent(content);
		} else {
			if (methodReturnType.getPackage() == null
					// arrays have no package, do not omit arrays
					&& !(methodReturnType instanceof CtArrayTypeReference<?>)) {
				// e.g. for ? generic capture
				logger.info("Ignoring methodReturnType {}", methodReturnType.getSimpleName());
				methodReturnType = new TypeFactory().OMITTED_TYPE_ARG_TYPE;
				methodReturnType.setSimpleName(UNSPECIFIED_SIMPLE_NAME);
			}

			MediaType mediaType = schemaGeneratorHelper.createMediaType(methodReturnType, null, getGenericParams(methodReturnType));
			if (mediaType != null) { // mediaType might be null, e.g., if the returnType is not part of the project (e.g., java.util.Map for delete).
				Content content = new Content();
				content.addMediaType(StringUtils.isBlank(produces) ? resolveDefaultContentType(methodReturnType) : produces, mediaType);
				apiResponse.setContent(content);
			}
		}

		// create the API response
		HttpStatus responseStatusCode = tryResolveResponseStatus(method);
		if (responseStatusCode == null){
//			if (apiResponse.getContent() != null)
				responseStatusCode = DEFAULT_RESPONSE_STATUS;
//			else
//				responseStatusCode = HttpStatus.NO_CONTENT;
		}

		apiResponse.setDescription(responseStatusCode.getReasonPhrase());

		ApiResponses apiResponses = new ApiResponses();
		apiResponses.put(String.valueOf(responseStatusCode.value()), apiResponse);
		return apiResponses;
	}

	/**
	 * Strips the Spring return value wrappers, e.g., ResponseEntity&lt;T&gt; or DeferredResult&lt;T&gt;,
	 * and returns the generic type T or OMITTED_TYPE_ARG_TYPE.
	 * @param methodReturnType
	 * @return
	 */
	private static CtTypeReference<?> stripReturnValueWrapper(CtTypeReference<?> methodReturnType) {
		// check for generic type
		if (!methodReturnType.getActualTypeArguments().isEmpty())
			methodReturnType = methodReturnType.getActualTypeArguments().get(0);
		else {
			// ignoring empty ResponseEntity capture
			methodReturnType = new TypeFactory().OMITTED_TYPE_ARG_TYPE;
			methodReturnType.setSimpleName(UNSPECIFIED_SIMPLE_NAME);
		}
		return methodReturnType;
	}

	private String resolveDefaultContentType(CtTypeReference<?> responseBody) {
		if (isFileResponse(responseBody)) {
			return DEFAULT_FILE_RETURN_CONTENT_TYPE;
		}
		return DEFAULT_CONTENT_TYPE;
	}

	private boolean isFileResponse(CtTypeReference<?> responseBodyClass) {
		return responseBodyClass.isSubtypeOf(new TypeFactory().get(restFramework.getSupportedFileType()).getReference());
	}

	private List<CtTypeReference<?>> getGenericParams(CtTypeReference<?> methodType) {
		return schemaGeneratorHelper.getGenericParams(methodType);
	}

	/**
	 * Trys to extract the response from the ResponseStatus or ApiResponse annotations.
	 * @param method
	 * @return Optional.empty if no annotation was found
	 */
	private HttpStatus tryResolveResponseStatus(CtMethod<?> method) {
		// TODO ApiResponses annotation

		ResponseStatus responseStatusSpringAnnotation = method.getAnnotation(ResponseStatus.class);
		if (responseStatusSpringAnnotation != null) {
			return HttpStatus.valueOf(defaultIfUnexpectedServerError(responseStatusSpringAnnotation.code(), responseStatusSpringAnnotation.value()).value());
		}

		io.swagger.v3.oas.annotations.responses.ApiResponse responseStatusSwaggerAnnotation = method.getAnnotation(io.swagger.v3.oas.annotations.responses.ApiResponse.class);
		if (responseStatusSwaggerAnnotation != null) {
			try {
				int responseCode = Integer.parseInt(getStatusCodeFromApiResponseAnnotation(responseStatusSwaggerAnnotation));
				return HttpStatus.valueOf(responseCode);
			} catch (NumberFormatException e) {
				return null;
			}
		}

		return null;
	}

	private String getStatusCodeFromApiResponseAnnotation(io.swagger.v3.oas.annotations.responses.ApiResponse response){
		String defaultVal;
		try {
			defaultVal = (String)io.swagger.v3.oas.annotations.responses.ApiResponse.class.getDeclaredMethod("responseCode").getDefaultValue();
		} catch (NoSuchMethodException e) {
			defaultVal = "default";
		}

		return response.responseCode().equals(defaultVal) ? response.description() : response.responseCode();
	}

	private HttpStatus defaultIfUnexpectedServerError(HttpStatus code, HttpStatus value) {
		// code default value is internal server error
		return code == HttpStatus.INTERNAL_SERVER_ERROR ? value : code;
	}

	private void mapGet(Annotation annotation, CtMethod<?> method, Map<String, PathItem> operationsMap, String controllerClassName, String baseControllerPath) {
		RequestAnnotation requestAnnotation = restFramework.convertToRequestAnnotation(annotation, method);
		logger.debug("Called mapGet with annotation name \"{}\", path {}, method {}, value {}, produces {}", requestAnnotation.name(), getFirstFromArray(requestAnnotation.path()), Arrays.toString(requestAnnotation.method()), getFirstFromArray(requestAnnotation.value()), getFirstFromArray(requestAnnotation.produces()));

		String path = ObjectUtils.defaultIfNull(getFirstFromArray(requestAnnotation.value()), getFirstFromArray(requestAnnotation.path()));
		String fullPath = prepareUrl(baseControllerPath, "/", path);
		String cleanedPath = removeRegexFromPath(fullPath);
		logger.debug("Base Controller Path {}, Path {}, fullPath {}, cleanedPath {}", baseControllerPath, path, fullPath, cleanedPath);

		Operation operation = new Operation();
		operation.setOperationId(getOperationId(cleanedPath, requestAnnotation.name(), method, HttpMethod.GET));
		operation.setSummary(!StringUtils.isBlank(requestAnnotation.name()) ? requestAnnotation.name() : method.getSimpleName());
		operation.setTags(singletonList(classNameToTag(controllerClassName)));

		operation.setParameters(transformParameters(fullPath, method));
		operation.setResponses(createApiResponses(method, getFirstFromArray(requestAnnotation.produces())));

		operationInterceptors.forEach(interceptor -> interceptor.intercept(method, operation));
		updateOperationsMap(cleanedPath, operationsMap, pathItem -> pathItem.setGet(operation));
	}

	private void mapPatch(Annotation annotation, CtMethod<?> method, Map<String, PathItem> operationsMap, String controllerClassName, String baseControllerPath) {
		RequestAnnotation requestAnnotation = restFramework.convertToRequestAnnotation(annotation,method);
		String path = ObjectUtils.defaultIfNull(getFirstFromArray(requestAnnotation.value()),
				getFirstFromArray(requestAnnotation.path()));
		String fullPath = prepareUrl(baseControllerPath, "/", path);
		String cleanedPath = removeRegexFromPath(fullPath);

		Operation operation = new Operation();
		operation.setOperationId(getOperationId(cleanedPath, requestAnnotation.name(), method, HttpMethod.PATCH));
		operation.setSummary(!StringUtils.isBlank(requestAnnotation.name()) ? requestAnnotation.name() : method.getSimpleName());
		operation.setTags(singletonList(classNameToTag(controllerClassName)));

		operation.setRequestBody(createRequestBody(method, getFirstFromArray(requestAnnotation.consumes())));
		operation.setResponses(createApiResponses(method, getFirstFromArray(requestAnnotation.produces())));
		operation.setParameters(transformParameters(fullPath, method));

		operationInterceptors.forEach(interceptor -> interceptor.intercept(method, operation));
		updateOperationsMap(cleanedPath, operationsMap, pathItem -> pathItem.setPatch(operation));
	}

	private void mapPut(Annotation annotation, CtMethod<?> method, Map<String, PathItem> operationsMap, String controllerClassName, String baseControllerPath) {
		RequestAnnotation requestAnnotation = restFramework.convertToRequestAnnotation(annotation, method);
		String path = ObjectUtils.defaultIfNull(getFirstFromArray(requestAnnotation.value()),
				getFirstFromArray(requestAnnotation.path()));
		String fullPath = prepareUrl(baseControllerPath, "/", path);
		String cleanedPath = removeRegexFromPath(fullPath);

		Operation operation = new Operation();
		operation.setOperationId(getOperationId(cleanedPath, requestAnnotation.name(), method, HttpMethod.PUT));
		operation.setSummary(!StringUtils.isBlank(requestAnnotation.name()) ? requestAnnotation.name() : method.getSimpleName());
		operation.setTags(singletonList(classNameToTag(controllerClassName)));

		operation.setRequestBody(createRequestBody(method, getFirstFromArray(requestAnnotation.consumes())));
		operation.setResponses(createApiResponses(method, getFirstFromArray(requestAnnotation.produces())));
		operation.setParameters(transformParameters(fullPath, method));

		operationInterceptors.forEach(interceptor -> interceptor.intercept(method, operation));
		updateOperationsMap(cleanedPath, operationsMap, pathItem -> pathItem.setPut(operation));
	}

	private void mapPost(Annotation annotation, CtMethod<?> method, Map<String, PathItem> operationsMap, String controllerClassName, String baseControllerPath) {
		RequestAnnotation requestAnnotation = restFramework.convertToRequestAnnotation(annotation, method);
		String path = ObjectUtils.defaultIfNull(getFirstFromArray(requestAnnotation.value()),
				getFirstFromArray(requestAnnotation.path()));
		String fullPath = prepareUrl(baseControllerPath, "/", path);
		String cleanedPath = removeRegexFromPath(fullPath);

		Operation operation = new Operation();
		operation.setOperationId(getOperationId(cleanedPath, requestAnnotation.name(), method, HttpMethod.POST));
		operation.setSummary(!StringUtils.isBlank(requestAnnotation.name()) ? requestAnnotation.name() : method.getSimpleName());
		operation.setTags(singletonList(classNameToTag(controllerClassName)));

		operation.setRequestBody(createRequestBody(method, getFirstFromArray(requestAnnotation.consumes())));
		operation.setResponses(createApiResponses(method, getFirstFromArray(requestAnnotation.produces())));
		operation.setParameters(transformParameters(fullPath, method));

		operationInterceptors.forEach(interceptor -> interceptor.intercept(method, operation));
		updateOperationsMap(cleanedPath, operationsMap, pathItem -> pathItem.setPost(operation));
	}

	private void updateOperationsMap(String url, Map<String, PathItem> existingMap, Consumer<PathItem> pathItemUpdater) {
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
	private List<io.swagger.v3.oas.models.parameters.Parameter> transformParameters(String endpointPath, CtMethod<?> method) {
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
			oasParameter.setRequired(true);
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

		oasParameter.setSchema(createSchemaFromParameter(parameter, parameterName));
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
		if (shouldBeIgnored(requestBodyParameter.getParameter())) {
			logger.info("Ignoring parameter {}", requestBodyParameter.getName());
			return null;
		}

		Content content = new Content();
		content.addMediaType(resolveContentType(userDefinedContentType, requestBodyParameter.getParameter()),
				schemaGeneratorHelper.createMediaType(
						requestBodyParameter.getParameter().getType(),
						requestBodyParameter.getName(),
						singletonList(getGenericParam(requestBodyParameter.getParameter().getType()))
				)
		);

		RequestBody requestBody = new RequestBody();
		requestBody.setRequired(true);
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

	private Schema createSchemaFromParameter(CtParameter<?> parameter, String parameterName) {
		CtTypeReference<?> clazz = parameter.getType();
		Annotation[] annotations = getActualAnnotations(parameter);

		return createSchema(clazz, annotations);
	}

	private Schema createSchema(CtTypeReference<?> parameterClass, Annotation[] annotations) {
		Schema schema;
		if (parameterClass.isPrimitive()) {
			schema = schemaGeneratorHelper.parseBaseTypeSignature(parameterClass, annotations);
		} else if (parameterClass.isArray()) {
			schema = schemaGeneratorHelper.parseArraySignature(parameterClass.getDeclaringType(), null, annotations);
		} else if (parameterClass.isSubtypeOf(new TypeFactory().get(List.class).getReference())) {
			var listGenericParameter = getGenericParam(parameterClass);
			schema = schemaGeneratorHelper.parseArraySignature(listGenericParameter, null, annotations);
		} else {
			schema = schemaGeneratorHelper.parseClassRefTypeSignature(parameterClass, annotations, null);
		}
		return schema;
	}

	private Annotation[] getActualAnnotations(CtParameter<?> parameter) {
		return schemaGeneratorHelper.getActualAnnotations(parameter.getAnnotations());
	}

	private String resolveContentType(String userDefinedContentType, CtParameter<?> requestBody) {
		if (StringUtils.isBlank(userDefinedContentType)) {
			return schemaGeneratorHelper.isFile(requestBody.getType()) ? MULTIPART_FORM_DATA_CONTENT_TYPE : DEFAULT_CONTENT_TYPE;
		}
		return userDefinedContentType;
	}

	private ParameterNamePair getRequestBody(CtMethod<?> method) {
		List<CtParameter<?>> parameters = method.getParameters();
		ParameterNamePair result;

		// Check if framework has explicit @RequestBody annotation such as Spring Boot
		if (restFramework.hasRequestBodyAnnotation()) {
			// Find the first parameter with a request body annotation
			result = parameters.stream()
					.filter(param -> param.getAnnotation(restFramework.getRequestBodyAnnotation()) != null)
					.findFirst()
					.map(param -> new ParameterNamePair(param.getSimpleName(), param))
					.orElse(null);
		} else {
			// Find the first parameter without specific REST binding annotations or framework-injected types
			result = parameters.stream()
					.filter(param -> !restFramework.hasRestParameterBindingAnnotation(param) && !restFramework.isRestFrameworkInjectedType(param.getType()))
					.findFirst()
					.map(param -> new ParameterNamePair(param.getSimpleName(), param))
					.orElse(null);
		}

		// fall back if nothing was found
		if (result == null) {
			result = parameters.stream()
					.filter(param -> schemaGeneratorHelper.isFile(param.getType()))
					.findFirst()
					.map(param -> new ParameterNamePair(param.getSimpleName(), param))
					.orElse(null);
		}

		return result;
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
				.map(requestAnnotation ->
						requestAnnotation.value().length > 0
								? getFirstFromArray(requestAnnotation.value())
								: getFirstFromArray(requestAnnotation.path())
				)
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
