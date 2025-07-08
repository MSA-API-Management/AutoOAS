package com.github.jrcodeza.schema.generator;

import at.aau.serg.frameworks.ValidationAnnotationProvider;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jrcodeza.schema.generator.filters.SchemaFieldFilter;
import com.github.jrcodeza.schema.generator.interceptors.SchemaFieldInterceptor;
import com.github.jrcodeza.schema.generator.model.CustomComposedSchema;
import com.github.jrcodeza.schema.generator.model.InheritanceInfo;
import com.github.jrcodeza.schema.generator.util.SchemaGeneratorHelper;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.jrcodeza.schema.generator.util.GeneratorUtils.shouldBeIgnored;


public class ComponentSchemaTransformer {

    private final List<SchemaFieldInterceptor> schemaFieldInterceptors;
    private AtomicReference<SchemaFieldFilter> schemaFieldFilter;
    private final SchemaGeneratorHelper schemaGeneratorHelper;

    private static final Logger logger = LoggerFactory.getLogger(ComponentSchemaTransformer.class);

    private final ValidationAnnotationProvider validationAnnotationProvider;

    public ComponentSchemaTransformer(List<SchemaFieldInterceptor> schemaFieldInterceptors,
                                      AtomicReference<SchemaFieldFilter> schemaFieldFilter,
                                      SchemaGeneratorHelper schemaGeneratorHelper,
                                      ValidationAnnotationProvider validationAnnotationProvider
    ) {
        this.schemaFieldInterceptors = schemaFieldInterceptors;
        this.schemaFieldFilter = schemaFieldFilter;
        this.schemaGeneratorHelper = schemaGeneratorHelper;
        this.validationAnnotationProvider = validationAnnotationProvider;
    }

    public Schema transformSimpleSchema(Class<?> clazz, Map<String, InheritanceInfo> inheritanceMap) {
        throw new NotImplementedException("This method used Reflection and is not used anymore");
    }

    public Schema transformSimpleSchema(CtType<?> clazz, Map<String, InheritanceInfo> inheritanceMap) {
        if (clazz.isEnum()) {
            return schemaGeneratorHelper.createEnumSchema(((CtEnum<?>) clazz).getEnumValues().stream().map(v -> v.getSimpleName()));
        }

        // else is object
        List<String> requiredFields = new ArrayList<>();

        Schema<?> schema = new Schema<>();
        schema.setType("object");
        schema.setProperties(getClassProperties(clazz, requiredFields));
        schemaGeneratorHelper.enrichWithTypeAnnotations(schema,
                schemaGeneratorHelper.getActualAnnotations(clazz.getAnnotations()));

        updateRequiredFields(schema, requiredFields);

        if (inheritanceMap.containsKey(clazz.getQualifiedName())) {
            Discriminator discriminator = createDiscriminator(inheritanceMap.get(clazz.getQualifiedName()));
            schema.setDiscriminator(discriminator);
            enrichWithDiscriminatorProperty(schema, discriminator);
        }
        if (clazz.getSuperclass() != null) {
            return traverseAndAddProperties(schema, inheritanceMap, clazz.getSuperclass().getTypeDeclaration(), clazz);
        }

        return schema;
    }

    public Schema transformExternalSchema(CtTypeReference<?> type) {
        Schema<?> schema = new Schema<>();
        schema.setType("object");
        schema.setExternalDocs(new ExternalDocumentation()
                .url(type.getPackage().getSimpleName())
                .description("external package")
        );

        return schema;
    }

    /**
     * Creates a special schema for unspecified types, eg ResponseEntity or ResponseEntity<\?>.
     *
     * @param type
     * @return
     */
    public Schema transformUnspecifiedSchema(CtTypeReference<?> type) {
        Schema<?> schema = new Schema<>();
        schema.setType("object");
        schema.setExternalDocs(new ExternalDocumentation()
                .url("unspecified") // mandatory OpenAPI property
                .description("Unspecified return type, e.g., ResponseEntity<?>")
        );

        return schema;
    }

    private Discriminator createDiscriminator(InheritanceInfo inheritanceInfo) {
        Map<String, String> discriminatorTypeMapping = inheritanceInfo.getDiscriminatorClassMap().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        Discriminator discriminator = new Discriminator();
        discriminator.setPropertyName(inheritanceInfo.getDiscriminatorFieldName());
        discriminator.setMapping(discriminatorTypeMapping);
        return discriminator;
    }

    private void updateRequiredFields(Schema schema, List<String> requiredFields) {
        if (requiredFields == null || requiredFields.isEmpty()) {
            return;
        }
        if (schema.getRequired() == null) {
            schema.setRequired(requiredFields);
            return;
        }
        schema.getRequired().addAll(requiredFields);
    }

    private void updateSchemaProperties(Schema schema, String propertyName, Schema propertyValue) {
        if (StringUtils.isBlank(propertyName) || propertyValue == null) {
            return;
        }
        if (schema.getProperties() == null) {
            schema.setProperties(new HashMap<>());
        }
        schema.getProperties().put(propertyName, propertyValue);
    }

    private void enrichWithDiscriminatorProperty(Schema schema, Discriminator discriminator) {
        if (schema != null && !schema.getProperties().containsKey(discriminator.getPropertyName())) {
            List<String> discriminatorTypeRequiredProperty = new ArrayList<>();
            discriminatorTypeRequiredProperty.add(discriminator.getPropertyName());

            updateSchemaProperties(schema, discriminator.getPropertyName(), new StringSchema());
            updateRequiredFields(schema, discriminatorTypeRequiredProperty);
        }
    }

    private Schema<?> traverseAndAddProperties(Schema<?> schema, Map<String, InheritanceInfo> inheritanceMap,
                                               CtType<?> superclass, CtType<?> actualClass) {
        if (schemaGeneratorHelper.isInPackagesToBeScanned(superclass)) {
            // create a composed schema with allOf
            //  as explained in https://swagger.io/docs/specification/data-models/inheritance-and-polymorphism/
            Schema<?> parentClassSchema = new Schema<>();
            parentClassSchema.set$ref(
                    schemaGeneratorHelper.prepareSchemaReference(superclass.getReference())
            );

            CustomComposedSchema composedSchema = new CustomComposedSchema();
            enrichWithAdditionalProperties(composedSchema, inheritanceMap, superclass.getQualifiedName(), actualClass.getSimpleName());
            composedSchema.setAllOf(Arrays.asList(parentClassSchema, schema));
            composedSchema.setDescription(schema.getDescription());
            return composedSchema;

        } else {
            // adding properties from parent classes is present due to swagger ui bug, after using different ui
            // this becomes relevant only for third party packages
            List<String> requiredFields = new ArrayList<>();
            schema.getProperties().putAll(getClassProperties(superclass, requiredFields));
            updateRequiredFields(schema, requiredFields);
            if (superclass.getSuperclass() != null && !"java.lang".equals(superclass.getSuperclass().getPackage().getSimpleName())) {
                return traverseAndAddProperties(schema, inheritanceMap, superclass.getSuperclass().getTypeDeclaration(), superclass);
            }
            return schema;
        }
    }

    private void enrichWithAdditionalProperties(CustomComposedSchema customComposedSchema, Map<String, InheritanceInfo> inheritanceMap,
                                                String superClassQualifiedName, String actualClassName) {
        if (inheritanceMap.containsKey(superClassQualifiedName)) {
            Map<String, String> discriminatorClassMap = inheritanceMap.get(superClassQualifiedName).getDiscriminatorClassMap();
            if (discriminatorClassMap.containsKey(actualClassName)) {
                customComposedSchema.setDiscriminatorValue(discriminatorClassMap.get(actualClassName));
            }
        }
    }

    private Map<String, Schema> getClassProperties(CtType<?> clazz, List<String> requiredFields) {
        Map<String, Schema> classPropertyMap = new HashMap<>();

        for (CtField<?> field : clazz.getFields()) {
            // dont consider static fields
            if (field.isStatic())
                continue;

            getFieldSchema(clazz, field, requiredFields).ifPresent(schema -> {
                schemaFieldInterceptors.forEach(modelClassFieldInterceptor -> modelClassFieldInterceptor.intercept(clazz, field, schema));
                classPropertyMap.put(field.getSimpleName(), schema);
            });
        }

        return classPropertyMap;
    }

    private Optional<Schema> getFieldSchema(CtType<?> clazz, CtField<?> field, List<String> requiredFields) {
        if (shouldIgnoreField(clazz, field)) {
            return Optional.empty();
        }

        CtTypeReference<?> typeSignature = field.getType();
        Annotation[] annotations = schemaGeneratorHelper.getActualAnnotations(field.getAnnotations());
        if (isRequired(annotations)) {
            requiredFields.add(field.getSimpleName());
        }

        // Translate variable names if @JsonProperty annotation exists
        for (Annotation annotation : annotations) {
            if (annotation instanceof JsonProperty jsonProperty) {
                String jsonPropertyValue = jsonProperty.value();

                if (jsonPropertyValue != null) {
                    logger.info("Found @JsonProperty with value: {}, replacing original field name: {}", jsonPropertyValue, field.getSimpleName());
                    field.setSimpleName(jsonPropertyValue);
                }
            }
        }

        if (typeSignature.isPrimitive()) {
            return createBaseTypeSchema(field, requiredFields, annotations);
        } else if (typeSignature.isArray()) {
            return createArrayTypeSchema(typeSignature, annotations);
        } else if (StringUtils.equalsIgnoreCase(typeSignature.getQualifiedName(), "java.lang.Object")) {
            ObjectSchema objectSchema = new ObjectSchema();
            objectSchema.setName(field.getSimpleName());
            return Optional.of(objectSchema);
        } else if (schemaGeneratorHelper.isTypeEquivalent(typeSignature, List.class)) {
            // if no parameterized types are available the getGenericParam returns null and parseSignature uses object as parameterized type
            CtTypeReference<?> listGenericParameter = schemaGeneratorHelper.getGenericParam(typeSignature);
            return Optional.of(schemaGeneratorHelper.parseArraySignature(listGenericParameter, annotations));
        } else if (schemaGeneratorHelper.isTypeEquivalent(typeSignature, Map.class)) {
            // if no parameterized types are available the getGenericParam returns null and parseSignature uses object as parameterized type
            CtTypeReference<?> listGenericParameter = schemaGeneratorHelper.getGenericParamAt(typeSignature, 1); // 0th is always object
            return Optional.of(schemaGeneratorHelper.parseDictSignature(listGenericParameter, annotations));
        } else {
            return createClassRefSchema(typeSignature, annotations);
        }
    }

    private boolean shouldIgnoreField(Class<?> clazz, Field field) {
        if (shouldBeIgnored(field)) {
            return true;
        }

        return schemaFieldFilter.get() != null && schemaFieldFilter.get().shouldIgnore(clazz, field);
    }

    private boolean shouldIgnoreField(CtType<?> clazz, CtField<?> field) {
        if (shouldBeIgnored(field)) {
            return true;
        }

        return schemaFieldFilter.get() != null && schemaFieldFilter.get().shouldIgnore(clazz, field);
    }

    private Optional<Schema> createClassRefSchema(CtTypeReference<?> typeClass, Annotation[] annotations) {
        Schema<?> schema = schemaGeneratorHelper.parseClassRefTypeSignature(typeClass, annotations);
        schemaGeneratorHelper.enrichWithTypeAnnotations(schema, annotations);
        return Optional.ofNullable(schema);
    }

    private Optional<Schema> createArrayTypeSchema(CtTypeReference<?> typeSignature, Annotation[] annotations) {
        CtTypeReference<?> arrayComponentType = schemaGeneratorHelper.getGenericParam(typeSignature);
        Schema<?> schema = schemaGeneratorHelper.parseArraySignature(arrayComponentType, annotations);
        schemaGeneratorHelper.enrichWithTypeAnnotations(schema, annotations);
        return Optional.ofNullable(schema);
    }

    private Optional<Schema> createBaseTypeSchema(CtField<?> field, List<String> requiredFields, Annotation[] annotations) {
        if (!requiredFields.contains(field.getSimpleName())) {
            requiredFields.add(field.getSimpleName());
        }
        Schema<?> schema = schemaGeneratorHelper.parseBaseTypeSignature(field.getType(), annotations);
        schemaGeneratorHelper.enrichWithTypeAnnotations(schema, annotations);
        return Optional.ofNullable(schema);
    }

    private boolean isRequired(Annotation[] annotations) {
        return Stream.of(annotations).anyMatch(annotation -> {
                    return validationAnnotationProvider.isNotNullAnnotation(annotation) || validationAnnotationProvider.isNotEmptyAnnotation(annotation);
//                        || (annotation instanceof io.swagger.v3.oas.annotations.media.Schema &&
//                                ((io.swagger.v3.oas.annotations.media.Schema) annotation).required())
                }
        );
    }

}
