package com.github.jrcodeza.schema.generator;

import at.aau.serg.frameworks.RestFramework;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import spoon.reflect.declaration.CtMethod;

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
        if (responseStatusCode == null){
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
     *
     * // todo HttpStatus / ResponseStatus is spring-specific
     *
     * @param method
     * @return Optional.empty if no annotation was found
     */
    private HttpStatus tryResolveResponseStatus(CtMethod<?> method) {
        // TODO ApiResponses annotation
        //  pretty sure that the ApiResponses are only documentation, not functional

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

        if (method.getType().getSimpleName().equals("void"))
        {
            return restFramework.getVoidMethodStatusCode();
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

    // todo HttpStatus is Spring-specific
    private HttpStatus defaultIfUnexpectedServerError(HttpStatus code, HttpStatus value) {
        // code default value is internal server error
        return code == HttpStatus.INTERNAL_SERVER_ERROR ? value : code;
    }
}
