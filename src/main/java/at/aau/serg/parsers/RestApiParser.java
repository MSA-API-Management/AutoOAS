package at.aau.serg.parsers;

import at.aau.serg.frameworks.RestFramework;
import at.aau.serg.frameworks.ValidationAnnotationProvider;
import at.aau.serg.frameworks.validation.ValidationAnnotationProviderFactory;
import at.aau.serg.openapi.OpenApiGenerator;
import com.github.jrcodeza.schema.generator.ComponentSchemaTransformer;
import com.github.jrcodeza.schema.generator.DataTypeTransformer;
import com.github.jrcodeza.schema.generator.OperationsTransformer;
import com.github.jrcodeza.schema.generator.model.InheritanceInfo;
import com.github.jrcodeza.schema.generator.util.SchemaGeneratorHelper;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import org.javatuples.Pair;
import spoon.MavenLauncher;
import spoon.OutputType;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class RestApiParser {
    private OperationsTransformer operationsTransformer;
    private DataTypeTransformer dataTypeTransformer;
    private ComponentSchemaTransformer schemaTransformer;
    private SchemaGeneratorHelper schemaHelper;

    private final OpenApiGenerator openApiGen = new OpenApiGenerator();

    protected CtModel model;
    protected String projectName;
    protected String outputFileName;
    private RestFramework restFramework;

    protected RestApiParser(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    protected RestApiParser(String projectPath, String outputFileName, RestFramework restFramework) {
        this(outputFileName);
        this.restFramework = restFramework;
        this.projectName = projectPath.substring(projectPath.lastIndexOf('/') + 1);
        this.model = new SpoonModelLoader().loadModel(projectPath);
    }

    protected RestApiParser(String projectPath, String outputFileName, RestFramework restFramework, CtModel model) {
        this(outputFileName);
        this.restFramework = restFramework;
        this.projectName = projectPath.substring(projectPath.lastIndexOf('/') + 1);
        this.model = model;
    }

    public void run() {
        generateOpenApi(model);
    }

    /**
     * Generates one OpenAPI per Spring Boot Profile from the project. Writes the OASs to file and returns them.
     *
     * @param model
     * @return
     */
    private List<OpenAPI> generateOpenApi(CtModel model) {
        var packages = model.getAllPackages();
        List<String> packageNames = packages.stream()
                .filter(p -> !p.isEmpty())
                .map(p -> p.toString())
                .collect(Collectors.toList());


        RelevantClasses relevantClasses = getRelevantClassesFromPackages(packages);
        List<CtType<?>> controllerClasses = relevantClasses.getControllerClasses();
        List<CtType<?>> controllerAdviceClasses = relevantClasses.getControllerAdviceClasses();
        List<CtType<?>> explicitModelClasses = relevantClasses.getExplicitModelClasses();


        ValidationAnnotationProvider annotationProvider = new ValidationAnnotationProviderFactory().getCompositeProvider();
        schemaHelper = new SchemaGeneratorHelper(packageNames, restFramework, annotationProvider); // just provide all packages of the project's module
        dataTypeTransformer = new DataTypeTransformer(restFramework, schemaHelper);
        operationsTransformer = new OperationsTransformer(schemaHelper, dataTypeTransformer,
                new ArrayList<>(), Collections.singletonList(restFramework.getOperationResponseCodeInterceptor(controllerAdviceClasses, dataTypeTransformer, schemaHelper)),
                new ArrayList<>(), new ArrayList<>(), new AtomicReference<>(), restFramework);
        schemaTransformer = new ComponentSchemaTransformer(new ArrayList<>(), new AtomicReference<>(), schemaHelper, annotationProvider);

        Map<String, List<CtType<?>>> controllerClassesPerProfile = restFramework.splitClassesOnProfiles(controllerClasses);
        System.out.println("Detected profiles: " + controllerClassesPerProfile.keySet());

        var res = new ArrayList<OpenAPI>(controllerClassesPerProfile.size());

        for (var profile : controllerClassesPerProfile.entrySet()) {
            OpenAPI openApiForProfile = createOpenAPIFromProfile(profile, explicitModelClasses);

            if (openApiForProfile != null) {
                res.add(openApiForProfile);
            }
        }

        return res;
    }

    private OpenAPI createOpenAPIFromProfile(Map.Entry<String, List<CtType<?>>> profile, List<CtType<?>> explicitModelClasses) {
        String currentProfileName = profile.getKey();
        var controllerClassesForCurrentProfile = profile.getValue();

        if (controllerClassesForCurrentProfile.isEmpty()) {
            System.out.println("Skipping empty profile: " + currentProfileName);
            return null;
        } else
            return createOpenAPIFromControllers(currentProfileName, controllerClassesForCurrentProfile, explicitModelClasses);

    }

    private OpenAPI createOpenAPIFromControllers(String profileName, List<CtType<?>> controllerClasses, List<CtType<?>> explicitModelClasses) {
        Paths paths = createPathsFromControllers(controllerClasses);

        // after all the paths are generated, we know about the referenced models
        // todo remove this global variable dependency to schemaHelper (filled by OperationTransformer)
        Set<CtTypeReference<?>> modelClasses = schemaHelper.referencedModelClasses;
        modelClasses.addAll(explicitModelClasses.stream().map(CtType::getReference).collect(Collectors.toUnmodifiableSet()));
        Components components = createComponentsSchemasFromModels(modelClasses);

        Info info = openApiGen.getDummyInfo(projectName, restFramework.getOpenApiInfoDescription(profileName));

        OpenAPI openApi = openApiGen.createOpenApi(info, paths, components);

        writeOpenApiToFile(openApi, profileName);

        return openApi;
    }

    private void writeOpenApiToFile(OpenAPI openApi, String profileName) {
        var fileName = outputFileName.replace(".json", "") + "_" + profileName + ".json";
        openApiGen.writeOpenApiToFile(openApi, fileName);
        System.out.println("Wrote OpenAPI to " + fileName);
    }

    private Paths createPathsFromControllers(List<CtType<?>> controllerClasses) {
        Paths operationsMap = new Paths();

        // contains the concreteType (most concrete implementation class) and currentType (iteratively towards super).
        List<Pair<CtType, CtType>> notProcessedControllerClasses =
                controllerClasses.stream().map(t -> Pair.with((CtType) t, (CtType) t)).collect(Collectors.toList());

        while (notProcessedControllerClasses.size() > 0) {
            var curControllerClasses = new ArrayList<>(notProcessedControllerClasses);

            for (Pair<CtType, CtType> typePair : curControllerClasses) {

                CtType<?> concreteType = typePair.getValue0();
                CtType<?> currentType = typePair.getValue1();

                for (CtMethod<?> method : currentType.getMethods()) {
                    // Adds the operation for the method to the operationsMap
                    operationsTransformer.createOperation(
                            method, operationsTransformer.getBaseControllerPath(concreteType),
                            operationsMap, concreteType.getSimpleName());
                }

                if (currentType.getSuperclass() != null)
                    notProcessedControllerClasses.add(Pair.with(concreteType, currentType.getSuperclass().getTypeDeclaration()));
            }

            notProcessedControllerClasses.removeAll(curControllerClasses);
        }

        operationsTransformer.fixDuplicateOperationIds(operationsMap);

        return operationsMap;
    }

    /**
     * Creates the schemas for all transitive referenced models starting with modelClasses.
     *
     * @param modelClasses
     * @return
     */
    private Components createComponentsSchemasFromModels(Set<CtTypeReference<?>> modelClasses) {
        // The modelClasses are extended when executing the schemaTransformer.
        // Hence, the schemaHelper used for the operationsTransformer must be used for the schemaTransformer.

        // we now use explicit model classes also
        // assert modelClasses == schemaHelper.referencedModelClasses;

        Map<String, Schema> schemaMap = new HashMap<>();
        Map<String, InheritanceInfo> inheritanceMap = new HashMap<>();

        Set<CtTypeReference<?>> processedClasses = new HashSet<>();
        while (modelClasses.size() > 0) {
            // process all model classes in the set
            for (CtTypeReference<?> modelClassRef : new ArrayList<>(modelClasses)) {
                Schema<?> transformedComponentSchema;
                CtType<?> modelClass = modelClassRef.getTypeDeclaration();
                if (modelClass != null && schemaHelper.isInPackagesToBeScanned(modelClass))
                    transformedComponentSchema = schemaTransformer.transformSimpleSchema(modelClass, inheritanceMap);
                else if (modelClassRef.getSimpleName().equals(DataTypeTransformer.UNSPECIFIED_SIMPLE_NAME)) {
                    // ignored on purpose during path generation
                    transformedComponentSchema = schemaTransformer.transformUnspecifiedSchema(modelClassRef);
                } else {
                    // happens if the type is not defined inside the project, e.g., org.springframework.web.servlet.ModelAndView
                    transformedComponentSchema = schemaTransformer.transformExternalSchema(modelClassRef);
                }

                schemaMap.put(modelClassRef.getSimpleName(), transformedComponentSchema);
                processedClasses.add(modelClassRef);
            }
            // during processing the set is extended with newly encountered model classes in schemaHelper
            // remove the processed classes from it
            modelClasses.removeAll(processedClasses);
        }

        // restore the original + transitive set
        modelClasses.addAll(processedClasses);

        Components components = new Components();
        components.setSchemas(schemaMap);
        return components;
    }

    // TODO getControllerAnnotations, AdviceAnnotations, ModelSchemaAnnotations
    protected RelevantClasses getRelevantClassesFromPackages(Collection<CtPackage> packages) {
        List<CtType<?>> controllerClasses = new LinkedList<>();
        List<CtType<?>> controllerAdviceClasses = new LinkedList<>();
        List<CtType<?>> explicitModelClasses = new LinkedList<>();

        for (CtPackage pkg : packages) {
            for (CtType<?> type : pkg.getTypes()) {
                for (CtAnnotation<?> annotation : type.getAnnotations()) {
                    String annotationName = annotation.getAnnotationType().toString();
                    if (annotationName != null && this.restFramework.getControllerAnnotations().contains(annotationName)) {
                        controllerClasses.add(type);
                        break; // annotations
                    }

                    if (annotationName != null && this.restFramework.isGlobalExceptionHandler(annotationName, type)) {
                        controllerAdviceClasses.add(type);
                        break; // annotations
                    }

                    if (annotationName != null && this.restFramework.getModelSchemaAnnotations().contains(annotationName)) {
                        explicitModelClasses.add(type);
                        break; // annotations
                    }
                }
            }
        }

        return new RelevantClasses(controllerClasses, controllerAdviceClasses, explicitModelClasses);
    }
}
