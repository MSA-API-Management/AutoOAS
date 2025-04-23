package at.aau.serg.parsers;

import at.aau.serg.frameworks.RestFramework;
import at.aau.serg.openapi.OpenApiGenerator;
import com.github.jrcodeza.schema.generator.ComponentSchemaTransformer;
import com.github.jrcodeza.schema.generator.OperationsTransformer;
import com.github.jrcodeza.schema.generator.model.InheritanceInfo;
import com.github.jrcodeza.schema.generator.util.SchemaGeneratorHelper;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Schema;
import org.javatuples.Pair;
import spoon.MavenLauncher;
import spoon.OutputType;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class RestApiParser {
    private OperationsTransformer operationsTransformer;
    private ComponentSchemaTransformer schemaTransformer;
    private SchemaGeneratorHelper schemaHelper;

    private final OpenApiGenerator openApiGen = new OpenApiGenerator();

    protected CtModel model;
    protected String projectName;
    protected String outputFileName;
    private RestFramework restFramework;

    // todo jar arg
    protected boolean deleteSpoonTmpFile = true;

    protected RestApiParser(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    protected RestApiParser(String projectPath, String outputFileName, RestFramework restFramework) {
        this(outputFileName);
        this.restFramework = restFramework;
        this.projectName = projectPath.substring(projectPath.lastIndexOf('/') + 1);
        this.model = loadModel(projectPath);
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


        schemaHelper = new SchemaGeneratorHelper(packageNames, restFramework); // just provide all packages of the project's module
        operationsTransformer = new OperationsTransformer(schemaHelper,
                new ArrayList<>(), Collections.singletonList(restFramework.getOperationResponseCodeInterceptor(controllerAdviceClasses)),
                new ArrayList<>(), new ArrayList<>(), new AtomicReference<>(), restFramework);
        schemaTransformer = new ComponentSchemaTransformer(new ArrayList<>(), new AtomicReference<>(), schemaHelper);


        Map<String, List<CtType<?>>> controllerClassesPerProfile = splitClassesOnProfiles(controllerClasses);

        System.out.println("Detected Spring profiles: " + controllerClassesPerProfile.keySet());

        var res = new ArrayList<OpenAPI>(controllerClassesPerProfile.size());

        for (var profile : controllerClassesPerProfile.entrySet()) {
            String currentProfileName = profile.getKey();
            var controllerClassesForCurrentProfile = profile.getValue();

            if (controllerClassesForCurrentProfile.isEmpty()) {
                System.out.println("Skipping empty profile: " + currentProfileName);
                continue;
            }

            res.add(createOpenAPIFromControllers(currentProfileName, controllerClassesForCurrentProfile, explicitModelClasses));
        }

        return res;
    }

    private OpenAPI createOpenAPIFromControllers(String springProfileName, List<CtType<?>> controllerClasses, List<CtType<?>> explicitModelClasses) {
        Paths paths = createPathsFromControllers(controllerClasses);

        // after all the paths are generated, we know about the referenced models
        Set<CtTypeReference<?>> modelClasses = schemaHelper.referencedModelClasses;
        modelClasses.addAll(explicitModelClasses.stream().map(CtType::getReference).collect(Collectors.toUnmodifiableSet()));
        Components components = createComponentsSchemasFromModels(modelClasses);

        // TODO spring renaming stuff
        OpenAPI openApi = openApiGen.createOpenApi(openApiGen.getDummyInfo(projectName, "Spring Profile: " + springProfileName), paths, components);

        var fileName = outputFileName.replace(".json", "") + "_" + springProfileName + ".json";
        openApiGen.writeOpenApiToFile(openApi, fileName);
        System.out.println("Wrote OpenAPI to " + fileName);

        return openApi;
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
                else if (modelClassRef.getSimpleName().equals(OperationsTransformer.UNSPECIFIED_SIMPLE_NAME)) {
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

    protected Map<String, List<CtType<?>>> splitClassesOnProfiles(List<CtType<?>> controllerClasses) {
        // split the classes based on spring profile annotations
        Map<String, List<CtType<?>>> controllerClassesPerProfile = new HashMap<>();
        List<CtType<?>> controllerClassesInDefaultProfile = new ArrayList<>();

        for (CtType<?> clazz : controllerClasses) {
            boolean profileAnnotationFound = false;

            for (CtAnnotation<? extends Annotation> annotation : clazz.getAnnotations()) {
                if (this.restFramework.getProfileAnnotation().equals(annotation.getAnnotationType().toString())) {
                    profileAnnotationFound = true;
                    // add to annotated profiles
                    String[] profiles = (String[]) annotation.getValueAsObject("value");
                    for (String profile : profiles) {
                        controllerClassesPerProfile.putIfAbsent(profile, new ArrayList<>());
                        controllerClassesPerProfile.get(profile).add(clazz);
                    }
                    break;
                }
            }

            if (!profileAnnotationFound) {
                controllerClassesInDefaultProfile.add(clazz);
            }
        }

        // add all classes without profile to each explicit profile
        controllerClassesPerProfile.forEach((k, v) -> v.addAll(controllerClassesInDefaultProfile));

        // also consider the default profile classes alone (e.g., if no profiles exist)
        controllerClassesPerProfile.put("default", controllerClassesInDefaultProfile);

        return controllerClassesPerProfile;
    }

    // TODO getControllerAnnotations, AdviceAnnotations, ModelSchemaAnnotations
    protected RelevantClasses getRelevantClassesFromPackages(Collection<CtPackage> packages) {
        List<CtType<?>> controllerClasses = new LinkedList<>();
        List<CtType<?>> controllerAdviceClasses = new LinkedList<>();
        List<CtType<?>> explicitModelClasses = new LinkedList<>();

        for (CtPackage pkg : packages)
            for (CtType<?> type : pkg.getTypes())
                for (CtAnnotation<?> annotation : type.getAnnotations()) {
                    String annotationName = annotation.getAnnotationType().toString();
                    if (annotationName != null && this.restFramework.getControllerAnnotations().contains(annotationName)) {
                        controllerClasses.add(type);
                        break; // annotations
                    }

                    if (annotationName != null && this.restFramework.getControllerAdviceAnnotations().contains(annotationName)) {
                        controllerAdviceClasses.add(type);
                        break; // annotations
                    }

                    if (annotationName != null && this.restFramework.getModelSchemaAnnotations().contains(annotationName)) {
                        explicitModelClasses.add(type);
                        break; // annotations
                    }
                }

        return new RelevantClasses(controllerClasses, controllerAdviceClasses, explicitModelClasses);
    }

    /**
     * @author Christian
     */
    private CtModel loadModel(String path) {
        System.out.println("Loading model: " + path);

        if (deleteSpoonTmpFile) {
            new File(path + "/spoon.classpath-app.tmp").delete();
        }

        MavenLauncher launcher = new MavenLauncher(path, MavenLauncher.SOURCE_TYPE.APP_SOURCE);
        launcher.getEnvironment().setComplianceLevel(11);
        launcher.getEnvironment().setOutputType(OutputType.COMPILATION_UNITS);
        launcher.getEnvironment().setNoClasspath(true);

        launcher.buildModel();
        return launcher.getModel();
    }
}
