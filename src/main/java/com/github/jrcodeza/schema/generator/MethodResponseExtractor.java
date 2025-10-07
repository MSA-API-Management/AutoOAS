package com.github.jrcodeza.schema.generator;

import at.aau.serg.frameworks.RestFramework;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;

public class MethodResponseExtractor {
    private RestFramework restFramework;
    private DataTypeTransformer dataTypeTransformer;

    public MethodResponseExtractor(RestFramework restFramework, DataTypeTransformer dataTypeTransformer) {
        this.restFramework = restFramework;
        this.dataTypeTransformer = dataTypeTransformer;
    }

    public ApiResponses createApiResponses(CtMethod<?> method, String produces) {
        // todo merge logic for DeferredResult, ResponseEntity stripping

        // method.getType uses the method's return value's type, quite naive response type identification
        //  this works well for Spring, because it defines the detailed type
        ApiResponse apiResponse = dataTypeTransformer.detectAndCreateApiResponseContent(method.getType(), produces);

        // create the API response
        HttpStatus responseStatusCode = tryResolveResponseStatus(method);
        if (responseStatusCode == null) {
//			if (apiResponse.getContent() != null)
            responseStatusCode = restFramework.getDefaultResponseCode();
//			else
//				responseStatusCode = HttpStatus.NO_CONTENT;
        }

        apiResponse.setDescription(responseStatusCode.getReasonPhrase());

        ApiResponses apiResponses = new ApiResponses();
        apiResponses.put(String.valueOf(responseStatusCode.value()), apiResponse);
        return apiResponses;
    }

    /**
     * Trys to extract the response from the ResponseStatus or ApiResponse annotations.
     * <p>
     *
     * @param method
     * @return Optional.empty if no annotation was found
     */
    private HttpStatus tryResolveResponseStatus(CtMethod<?> method) {
        ResponseStatus responseStatusSpringAnnotation = method.getAnnotation(ResponseStatus.class);
        if (responseStatusSpringAnnotation != null) {
            return HttpStatus.valueOf(defaultIfUnexpectedServerError(responseStatusSpringAnnotation.code(), responseStatusSpringAnnotation.value()).value());
        }

        // FIXME ApiResponses annotation
        //  pretty sure that the ApiResponses are only documentation, not functional
        io.swagger.v3.oas.annotations.responses.ApiResponse responseStatusSwaggerAnnotation = method.getAnnotation(io.swagger.v3.oas.annotations.responses.ApiResponse.class);
        if (responseStatusSwaggerAnnotation != null) {
            try {
                int responseCode = Integer.parseInt(getStatusCodeFromApiResponseAnnotation(responseStatusSwaggerAnnotation));
                return HttpStatus.valueOf(responseCode);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // TODO proper asyncResponse and server sent event handling
        if (method.getType().getSimpleName().equals("void")) {
            for (CtParameter<?> parameter : method.getParameters()) {
                String parameterType = parameter.getType().toString();

                if (hasAnnotation(parameter, "Suspended") &&
                        parameterType.endsWith("AsyncResponse")) {
                    return restFramework.getDefaultResponseCode();
                }

                if (hasAnnotation(parameter, "Context") &&
                        parameterType.endsWith("SseEventSink")) {
                    return restFramework.getDefaultResponseCode();
                }
            }
            return restFramework.getVoidMethodStatusCode();
        }

        return null;
    }

    private boolean hasAnnotation(CtParameter<?> parameter, String annotationName) {
        return parameter.getAnnotations().stream()
                .anyMatch(annotation -> annotation.getAnnotationType().getSimpleName().equals(annotationName));
    }

    private String getStatusCodeFromApiResponseAnnotation(io.swagger.v3.oas.annotations.responses.ApiResponse response) {
        String defaultVal;
        try {
            defaultVal = (String) io.swagger.v3.oas.annotations.responses.ApiResponse.class.getDeclaredMethod("responseCode").getDefaultValue();
        } catch (NoSuchMethodException e) {
            defaultVal = "default";
        }

        return response.responseCode().equals(defaultVal) ? response.description() : response.responseCode();
    }

    private HttpStatus defaultIfUnexpectedServerError(HttpStatus code, HttpStatus value) {
        // code default value is internal server error
        return code == HttpStatus.INTERNAL_SERVER_ERROR ? value : code;
    }
}
