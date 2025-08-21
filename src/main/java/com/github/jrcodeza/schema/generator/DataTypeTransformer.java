package com.github.jrcodeza.schema.generator;

import at.aau.serg.frameworks.RestFramework;
import com.github.jrcodeza.schema.generator.util.SchemaGeneratorHelper;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;

import java.lang.annotation.Annotation;
import java.util.List;

public class DataTypeTransformer {

    public static final String UNSPECIFIED_SIMPLE_NAME = "UNSPECIFIED_TYPE";

    private static final String DEFAULT_CONTENT_TYPE = "application/json";
    private static final String DEFAULT_FILE_RETURN_CONTENT_TYPE = "application/octet-stream";
    private static final String MULTIPART_FORM_DATA_CONTENT_TYPE = "multipart/form-data";

    private static Logger logger = LoggerFactory.getLogger(OperationsTransformer.class);

    private RestFramework restFramework;
    private SchemaGeneratorHelper schemaGeneratorHelper;

    public DataTypeTransformer(RestFramework restFramework, SchemaGeneratorHelper schemaGeneratorHelper) {
        this.restFramework = restFramework;
        this.schemaGeneratorHelper = schemaGeneratorHelper;
    }

    public ApiResponse detectAndCreateApiResponseContent(CtTypeReference<?> responseType) {
        return detectAndCreateApiResponseContent(responseType, null);
    }

    // TODO alex: use this inside the interceptor for finding the type in .entity or .ok
    public ApiResponse detectAndCreateApiResponseContent(CtTypeReference<?> responseType, String produces) {
        ApiResponse apiResponse = new ApiResponse();

        // content

        // strip the DeferredResult wrapper
        if (schemaGeneratorHelper.isTypeEquivalent(responseType, restFramework.getAsyncResultWrapper())) {
            responseType = stripReturnValueWrapper(responseType);
        }

        // strip the ResponseEntity wrapper
        if (schemaGeneratorHelper.isTypeEquivalent(responseType, restFramework.getResponseWrapper())) {
            responseType = stripReturnValueWrapper(responseType);
        }

        if (schemaGeneratorHelper.isTypeEquivalent(responseType, restFramework.getOptionalWrapper())) {
            responseType = stripReturnValueWrapper(responseType);
        }

        if ("void".equals(responseType.getSimpleName())) {
            // dont add a content

        } else if (responseType.isPrimitive() ||
                (responseType.getPackage() != null && responseType.getPackage().getSimpleName().equals("java.lang"))) {
            Content content = new Content();
            MediaType simpleMediaType = new MediaType();
            simpleMediaType.setSchema(
                    responseType.isPrimitive()
                            ? schemaGeneratorHelper.parseBaseTypeSignature(responseType, new Annotation[0])
                            : schemaGeneratorHelper.parseClassRefTypeSignature(responseType, new Annotation[0], null)
            );
            content.addMediaType(StringUtils.isBlank(produces) ? resolveDefaultContentType(responseType) : produces, simpleMediaType);
            apiResponse.setContent(content);

        } else {
            if (responseType.getPackage() == null
                    // arrays have no package, do not omit arrays
                    && !(responseType instanceof CtArrayTypeReference<?>)) {
                // e.g. for ? generic capture
                logger.info("Ignoring responseType {}", responseType.getSimpleName());
                responseType = new TypeFactory().OMITTED_TYPE_ARG_TYPE;
                responseType.setSimpleName(UNSPECIFIED_SIMPLE_NAME);
            }

            MediaType mediaType = schemaGeneratorHelper.createMediaType(responseType, null);
            if (mediaType != null) { // mediaType might be null, e.g., if the returnType is not part of the project (e.g., java.util.Map for delete).
                Content content = new Content();
                content.addMediaType(StringUtils.isBlank(produces) ? resolveDefaultContentType(responseType) : produces, mediaType);
                apiResponse.setContent(content);
            }
        }

        return apiResponse;
    }

    /**
     * Strips the Spring return value wrappers, e.g., ResponseEntity&lt;T&gt; or DeferredResult&lt;T&gt;,
     * and returns the generic type T or OMITTED_TYPE_ARG_TYPE.
     *
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


    public String resolveContentType(String userDefinedContentType, CtParameter<?> requestBody) {
        if (StringUtils.isBlank(userDefinedContentType)) {
            return schemaGeneratorHelper.isFile(requestBody.getType()) ? MULTIPART_FORM_DATA_CONTENT_TYPE : DEFAULT_CONTENT_TYPE;
        }
        return userDefinedContentType;
    }
}
