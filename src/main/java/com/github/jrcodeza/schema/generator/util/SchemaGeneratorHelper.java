package com.github.jrcodeza.schema.generator.util;

import at.aau.serg.frameworks.RestFramework;
import at.aau.serg.frameworks.ValidationAnnotationProvider;
import at.aau.serg.util.Utils;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.jrcodeza.schema.generator.util.CommonConstants.COMPONENT_REF_PREFIX;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class SchemaGeneratorHelper {
    private final RestFramework restFramework;
    private final ValidationAnnotationProvider validationAnnotationProvider;
    private static Logger logger = LoggerFactory.getLogger(SchemaGeneratorHelper.class);

    private final List<String> modelPackages;

    public Set<CtTypeReference<?>> referencedModelClasses = new HashSet<>();

    public SchemaGeneratorHelper(List<String> modelPackages, RestFramework restFramework) {
        this.modelPackages = modelPackages;
        this.restFramework = restFramework;
        this.validationAnnotationProvider = restFramework.getValidationAnnotationProvider();
    }

    public MediaType createMediaType(CtTypeReference<?> requestBodyType,
                                     String parameterName,
                                     List<CtTypeReference<?>> genericParams) {

        // todo merge DeferredResult, ResponseEntity handling logic
        if (requestBodyType.isSubtypeOf(new TypeFactory().get(restFramework.getAsyncResultWrapper()).getReference())) {
            // handle DeferredResult Spring wrapper, potentially containing everything
            System.out.println("Stripping DeferredResult, this should not be needed");
            if (!Utils.isEmpty(genericParams)) {
                // strip DeferredResult and get ResponseEntity
                requestBodyType = genericParams.get(0);
                genericParams = getGenericParams(genericParams.get(0));
            } else {
                System.out.println("Unknown return type wrapped by DeferredResult");
                return null;
            }
        }

        requestBodyType = unwrapFrameworkWrapper(requestBodyType, genericParams);
        Schema<?> rootMediaSchema = new Schema<>();

        if (isFile(requestBodyType)) {
            Schema<?> fileSchema = new Schema<>();
            fileSchema.setType("string");
            fileSchema.setFormat("binary");

            if (parameterName == null) {
                rootMediaSchema = fileSchema;
            } else {
                Map<String, Schema> properties = new HashMap<>();
                properties.put(parameterName, fileSchema);
                rootMediaSchema.setType("object");
                rootMediaSchema.setProperties(properties);
            }

        } else if (isCollection(requestBodyType, genericParams)) {
            rootMediaSchema = parseArraySignature(getFirstOrNull(genericParams), null, new Annotation[]{});

        } else if (requestBodyType instanceof CtArrayTypeReference<?>) {
            rootMediaSchema = parseArraySignature(((CtArrayTypeReference<?>) requestBodyType).getComponentType(), null, new Annotation[]{});

        } else if (isTypeEquivalent(requestBodyType, Map.class)) {
            rootMediaSchema = parseDictSignature(getGenericParamAt(requestBodyType, 1), new Annotation[]{});

        } else if (!StringUtils.equalsIgnoreCase(requestBodyType.getSimpleName(), "void")) {
            rootMediaSchema = parseClassRefTypeSignature(requestBodyType, new Annotation[]{});
        } else {
            // void
            return null;
        }

        MediaType mediaType = new MediaType();
        mediaType.setSchema(rootMediaSchema);
        return mediaType;
    }

    /**
     * Prepares the $ref field value, i.e., adds components/schemas as prefix to the name.
     * Additionally, the model name is stored for later analysis.
     *
     * @param modelClassName
     * @return
     */
    public String prepareSchemaReference(CtTypeReference<?> modelClassName) {
        referencedModelClasses.add(modelClassName);
        return COMPONENT_REF_PREFIX + modelClassName.getSimpleName();
    }


    private CtTypeReference<?> getFirstOrNull(List<CtTypeReference<?>> genericParams) {
        if (Utils.isEmpty(genericParams)) { // || genericParams.get(0).isAssignableFrom(List.class)) {
            return null;
        }
        return genericParams.get(0);
    }

    private boolean isCollection(CtTypeReference<?> requestBodyParameter, List<CtTypeReference<?>> genericTypes) {
        var potentialListType = unwrapFrameworkWrapper(requestBodyParameter, genericTypes);
        return isTypeEquivalent(potentialListType, Collection.class);
    }

    /**
     * TODO update naming and check if it is equivalent to old impl. Return null instead of type?
     *   Previously, you could assume that the wrapper was always gone.
     *   Now, the method returns the original wrapper if it does not define the generic type T
     *
     * @param type
     * @param genericTypes
     * @return
     */
    private CtTypeReference<?> unwrapFrameworkWrapper(CtTypeReference<?> type, List<CtTypeReference<?>> genericTypes) {
        if (type.isSubtypeOf(new TypeFactory().get(this.restFramework.getResponseWrapper()).getReference())
                && !Utils.isEmpty(genericTypes)) {
            return genericTypes.get(genericTypes.size() - 1);
        }
        return type; // If no known wrapper is found, return the original type // todo <- this assumption is now wrong
    }

    public boolean isFile(CtTypeReference<?> type) {
        return type.isSubtypeOf(new TypeFactory().get(restFramework.getSupportedFileType()).getReference());
    }


    @SuppressWarnings("squid:S1192") // better in-place defined for better readability
    public Schema parseBaseTypeSignature(CtTypeReference<?> type, Annotation[] annotations) {
//        type.isSubtypeOf(new TypeFactory().get(byte.class).getReference())
        String typeName = type.getSimpleName();
        if (typeName.equals("byte") || typeName.equals("short") || typeName.equals("int")) {
            return createNumberSchema("integer", "int32", annotations);
        } else if (typeName.equals("long")) {
            return createNumberSchema("integer", "int64", annotations);
        } else if (typeName.equals("float")) {
            return createNumberSchema("number", "float", annotations);
        } else if (typeName.equals("double")) {
            return createNumberSchema("number", "double", annotations);
        } else if (typeName.equals("char")) {
            return createStringSchema(null, annotations);
        } else if (typeName.equals("boolean")) {
            return createBooleanSchema();
        }
        logger.info("Ignoring unsupported type=[{}]", type.getSimpleName());
        return null;
    }

    @SuppressWarnings("squid:S3776") // no other solution
    public Schema parseClassRefTypeSignature(CtTypeReference<?> typeClass,
                                             Annotation[] annotations) {
        return this.parseClassRefTypeSignature(typeClass, annotations, this.modelPackages);
    }

    @SuppressWarnings("squid:S3776") // no other solution
    public Schema parseClassRefTypeSignature(CtTypeReference<?> typeClass,
                                             Annotation[] annotations,
                                             List<String> modelPackages) {
        String typeName = typeClass.getSimpleName();
        if (typeName.equals("Byte") || typeName.equals("Short") || typeName.equals("Integer")) {
            return createNumberSchema("integer", "int32", annotations);
        } else if (typeName.equals("Long") || typeName.equals("BigInteger")) {
            return createNumberSchema("integer", "int64", annotations);
        } else if (typeName.equals("Float")) {
            return createNumberSchema("number", "float", annotations);
        } else if (typeName.equals("Double") || typeName.equals("BigDecimal")) {
            return createNumberSchema("number", "double", annotations);
        } else if (typeName.equals("Character") || typeName.equals("String")) {
            return createStringSchema(null, annotations);
        } else if (typeName.equals("Boolean")) {
            return createBooleanSchema();
        } else if (typeName.equals("List")) {
            return createListSchema(typeClass, modelPackages, annotations);
        } else if (typeName.equals("LocalDate") || typeName.equals("Date")) {
            return createStringSchema("date", annotations);
        } else if (typeName.equals("LocalDateTime") || typeName.equals("LocalTime")) {
            return createStringSchema("date-time", annotations);
        }
        return createRefSchema(typeClass, modelPackages);
    }

    public Schema parseArraySignature(CtTypeReference<?> elementTypeSignature,
                                      Annotation[] annotations) {
        return this.parseArraySignature(elementTypeSignature, this.modelPackages, annotations);
    }

    public Schema parseArraySignature(CtTypeReference<?> elementTypeSignature,
                                      List<String> modelPackages,
                                      Annotation[] annotations) {
        ArraySchema arraySchema = new ArraySchema();
        if (elementTypeSignature == null) {
            arraySchema.setItems(createObjectSchema());
            return arraySchema;
        }
        enrichWithTypeAnnotations(arraySchema, annotations);
        Stream.of(annotations).forEach(annotation -> applyArrayAnnotations(arraySchema, annotation));
        if (elementTypeSignature.isPrimitive()) {
            // primitive type like int
            Schema<?> itemSchema = new Schema<>();
            itemSchema.setType(mapBaseType(elementTypeSignature));
            arraySchema.setItems(itemSchema);
            return arraySchema;
        } else {
            String basicLangItemsType = mapBasicLangItemsType(elementTypeSignature);
            // basic types like Integer or String
            if (basicLangItemsType != null) {
                Schema<?> itemSchema = new Schema<>();
                itemSchema.setType(basicLangItemsType);
                arraySchema.setItems(itemSchema);
                return arraySchema;
            }
            // else do ref
            Schema<?> itemSchema = new Schema<>();
            itemSchema.set$ref(prepareSchemaReference(elementTypeSignature));
            arraySchema.setItems(itemSchema);
            return arraySchema;
        }
    }

    public Schema parseDictSignature(CtTypeReference<?> elementTypeSignature,
                                     Annotation[] annotations) {
        return this.parseDictSignature(elementTypeSignature, this.modelPackages, annotations);
    }

    public Schema parseDictSignature(CtTypeReference<?> elementTypeSignature,
                                     List<String> modelPackages,
                                     Annotation[] annotations) {
        Schema dictSchema = new Schema();
        dictSchema.setType("object");

        if (elementTypeSignature == null) {
            dictSchema.setAdditionalProperties(createObjectSchema());
            return dictSchema;
        }

        enrichWithTypeAnnotations(dictSchema, annotations);

        if (elementTypeSignature.isPrimitive()) {
            // primitive type like int
            Schema<?> addPropSchema = new Schema<>();
            addPropSchema.setType(mapBaseType(elementTypeSignature));
            dictSchema.setAdditionalProperties(addPropSchema);
            return dictSchema;

        } else {
            String basicLangItemsType = mapBasicLangItemsType(elementTypeSignature);
            // basic types like Integer or String
            if (basicLangItemsType != null) {
                Schema<?> addPropSchema = new Schema<>();
                addPropSchema.setType(basicLangItemsType);
                dictSchema.setAdditionalProperties(addPropSchema);
                return dictSchema;
            }
            // else do ref
            Schema<?> addPropSchema = new Schema<>();
            addPropSchema.set$ref(prepareSchemaReference(elementTypeSignature));
            dictSchema.setAdditionalProperties(addPropSchema);
            return dictSchema;
        }
    }

    private Schema<?> createObjectSchema() {
        Schema<?> schema = new Schema<>();
        schema.setType("object");
        return schema;
    }

    @SuppressWarnings("squid:S1192") // better in-place defined for better readability
    protected Schema createBooleanSchema() {
        Schema<?> schema = new Schema<>();
        schema.setType("boolean");
        return schema;
    }

    @SuppressWarnings("squid:S1192") // better in-place defined for better readability
    protected Schema createStringSchema(String format, Annotation[] annotations) {
        Schema<?> schema = new Schema<>();
        schema.setType("string");
        if (StringUtils.isNotBlank(format)) {
            schema.setFormat(format);
        }
        if (annotations != null) {
            asList(annotations).forEach(annotation -> applyStringAnnotations(schema, annotation));
        }
        return schema;
    }

    public StringSchema createEnumSchema(Stream<String> enumConstants) {
        StringSchema schema = new StringSchema();
        schema.setType("string");
        schema.setEnum(enumConstants.collect(Collectors.toList()));
        return schema;
    }

    protected Schema createNumberSchema(String type, String format, Annotation[] annotations) {
        Schema<?> schema = new Schema<>();
        schema.setType(type);
        schema.setFormat(format);
        asList(annotations).forEach(annotation -> applyNumberAnnotation(schema, annotation));
        return schema;
    }

    protected ComposedSchema createRefSchema(CtTypeReference<?> typeSignature, List<String> modelPackages) {
        ComposedSchema composedSchema = new ComposedSchema();

        composedSchema.set$ref(prepareSchemaReference(typeSignature));
        return composedSchema;
    }

    protected Schema createListSchema(CtTypeReference<?> typeSignature, List<String> modelPackages, Annotation[] annotations) {
        return parseArraySignature(typeSignature, modelPackages, annotations);
    }

    protected void applyStringAnnotations(Schema<?> schema, Annotation annotation) {
        validationAnnotationProvider.getPatternRegexpIfPresent(annotation)
                .ifPresent(schema::pattern);

        validationAnnotationProvider.getSizeMinIfPresent(annotation)
                .ifPresent(schema::minLength);

        validationAnnotationProvider.getSizeMaxIfPresent(annotation)
                .ifPresent(schema::maxLength);
    }

    protected void applyNumberAnnotation(Schema<?> schema, Annotation annotation) {
        validationAnnotationProvider.getDecimalMinValueIfPresent(annotation)
                .ifPresent(value -> schema.setMinimum(new BigDecimal(value)));

        validationAnnotationProvider.getDecimalMaxValueIfPresent(annotation)
                .ifPresent(value -> schema.setMaximum(new BigDecimal(value)));

        validationAnnotationProvider.getMinValueIfPresent(annotation)
                .ifPresent(value -> schema.setMinimum(new BigDecimal(value)));

        validationAnnotationProvider.getMaxValueIfPresent(annotation)
                .ifPresent(value -> schema.setMaximum(new BigDecimal(value)));
    }

    protected void applyArrayAnnotations(ArraySchema schema, Annotation annotation) {
        validationAnnotationProvider.getSizeMinIfPresent(annotation)
                .ifPresent(schema::minItems);

        validationAnnotationProvider.getSizeMaxIfPresent(annotation)
                .ifPresent(schema::maxItems);
    }

    protected String mapBasicLangItemsType(CtTypeReference<?> classRefTypeSignature) {
        String typeName = classRefTypeSignature.getSimpleName();
        if (typeName.equals("Byte") || typeName.equals("Short") || typeName.equals("Integer")
                || typeName.equals("Long") || typeName.equals("BigInteger")) {
            return "integer";
        } else if (typeName.equals("Float") || typeName.equals("Double") || typeName.equals("BigDecimal")) {
            return "number";
        } else if (typeName.equals("Character") || typeName.equals("String") || typeName.equals("LocalDate")
                || typeName.equals("Date") || typeName.equals("LocalDateTime")
                || typeName.equals("LocalTime")) {
            return "string";
        } else if (typeName.equals("Boolean")) {
            return "boolean";
        } else if (typeName.equals("List")) {
//            throw new IllegalArgumentException("Nested List types are not supported"
//                    + classRefTypeSignature.getSimpleName()
//            );
            // todo support nested lists
            return "list";
        }
        return null;
    }

    protected String mapBaseType(CtTypeReference<?> elementTypeSignature) {
        String typeName = elementTypeSignature.getSimpleName();
        if (typeName.equals("byte") || typeName.equals("short")
                || typeName.equals("int") || typeName.equals("long")) {
            return "integer";
        } else if (typeName.equals("float") || typeName.equals("double")) {
            return "number";
        } else if (typeName.equals("char")) {
            return "string";
        } else if (typeName.equals("boolean")) {
            return "boolean";
        }
        throw new IllegalArgumentException(format("Unsupported base type=[%s]", elementTypeSignature.getSimpleName()));
    }

    public boolean isInPackagesToBeScanned(CtTypeReference<?> clazz, List<String> modelPackages) {
        return isInPackagesToBeScanned(clazz.getTypeDeclaration(), modelPackages);
    }

    public boolean isInPackagesToBeScanned(CtTypeReference<?> clazz) {
        return isInPackagesToBeScanned(clazz.getTypeDeclaration());
    }

    public boolean isInPackagesToBeScanned(CtType<?> clazz) {
        return isInPackagesToBeScanned(clazz, this.modelPackages);
    }

    public boolean isInPackagesToBeScanned(CtType<?> clazz, List<String> modelPackages) {
        return modelPackages == null
                || modelPackages.stream().anyMatch(pkg -> clazz.getPackage().getQualifiedName().equals(pkg));
    }

    public void enrichWithTypeAnnotations(Schema<?> schema, Annotation[] annotations) {
        enrichWithAnnotation(io.swagger.v3.oas.annotations.media.Schema.class, annotations,
                schemaAnnotation -> {
                    schema.setDeprecated(schemaAnnotation.deprecated());
                    schema.setDescription(schemaAnnotation.description());
                    enrichWithAccessMode(schema, schemaAnnotation);
                });
        enrichWithAnnotation(Deprecated.class, annotations, deprecatedAnnotation -> schema.setDeprecated(true));
    }

    public void enrichWithTypeAnnotations(Parameter parameter, Annotation[] annotations) {
        enrichWithAnnotation(io.swagger.v3.oas.annotations.media.Schema.class, annotations,
                schemaAnnotation -> {
                    parameter.setDeprecated(schemaAnnotation.deprecated());
                    parameter.setDescription(schemaAnnotation.description());
                });
        enrichWithAnnotation(Deprecated.class, annotations, deprecatedAnnotation -> parameter.setDeprecated(true));
    }

    private void enrichWithAccessMode(Schema<?> schema, io.swagger.v3.oas.annotations.media.Schema schemaAnnotation) {
        if (schemaAnnotation.accessMode() == io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY) {
            schema.setReadOnly(true);
        } else if (schemaAnnotation.accessMode() == io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY) {
            schema.setWriteOnly(true);
        }
    }

    protected <T> void enrichWithAnnotation(Class<T> annotationClazz, Annotation[] annotations, Consumer<T> consumer) {
        Stream.of(annotations)
                .filter(annotation -> annotationClazz.isAssignableFrom(annotation.getClass()))
                .map(annotationClazz::cast)
                .findFirst()
                .ifPresent(consumer);
    }

    public List<CtTypeReference<?>> getGenericParams(CtTypeReference<?> methodType) {
        if (methodType.isParameterized()) {
            List<CtTypeReference<?>> typeArguments = methodType.getActualTypeArguments();
//			if (typeArguments != null && typeArguments.length > 0)
            {
                CtTypeReference<?> typeArgument = typeArguments.get(0);
                if (typeArgument.isClass()) {
                    return singletonList(typeArgument);
                } else if (typeArgument.isParameterized()) {
                    var innerTypes = typeArgument.getActualTypeArguments();
                    return innerTypes.size() > 0
                            ? asList(innerTypes.get(0), typeArgument)
                            : null;
                }
            }
        }
        return null;
    }

    @Deprecated
    public CtTypeReference<?> getGenericParam(CtParameter<?> parameter) {
        return getGenericParamAt(parameter.getType(), 0);

//        if (parameter.getType().getActualTypeArguments().size() > 0) {
//            CtTypeReference<?> parameterizedType = parameter.getType().getActualTypeArguments().get(0);
//            return parameterizedType;
//        }
//        return null;
    }

    public CtTypeReference<?> getGenericParam(CtTypeReference<?> type) {
        // idx = 0 required for getGenericParam(CtParameter) which was migrated to this call
        return getGenericParamAt(type, 0);
    }

    public CtTypeReference<?> getGenericParamAt(CtTypeReference<?> type, int idx) {
        if (type.getActualTypeArguments().size() > idx) {
            CtTypeReference<?> parameterizedType = type.getActualTypeArguments().get(idx);
            return parameterizedType;
        }
        return null;
    }

    /**
     * Also accepts subtypes
     *
     * @param ctType
     * @param type
     * @return
     */
    public boolean isTypeEquivalent(CtTypeReference<?> ctType, Class type) {
        return ctType != null && ctType.isSubtypeOf(new TypeFactory().get(type).getReference());
    }

    /**
     * Return an empty Annotation for each Annotation which cannot be parsed
     *
     * @param annotations
     * @return
     */
    public Annotation[] getActualAnnotations(List<CtAnnotation<? extends Annotation>> annotations) {
        return annotations.stream().map(a -> {
                    try {
                        return a.getActualAnnotation();
                    } catch (Exception e) {
                        return (Annotation) () -> null;
                    }
                }
        ).toArray(Annotation[]::new);
    }
}
