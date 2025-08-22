package at.aau.serg.parsers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import spoon.reflect.declaration.CtType;

@Getter
public class ControllerClassProcessingInformation {

    /**
     * The type of the controller used during processing, e.g., C extends S -> C
     */
    private CtType<?> concreteControllerType;

    /**
     * The type of the superclass used during processing, e.g., C extends S -> S
     */
    private CtType<?> currentSuperclassType;

    /**
     * The path prefix defined for the controller.
     */
    private String basePath;

    public ControllerClassProcessingInformation(CtType<?> concreteControllerType, CtType<?> currentSuperclassType) {
        this(concreteControllerType, currentSuperclassType, "");
    }

    public ControllerClassProcessingInformation(CtType<?> concreteControllerType, CtType<?> currentSuperclassType, String basePath) {
        this.concreteControllerType = concreteControllerType;
        this.currentSuperclassType = currentSuperclassType;
        this.basePath = basePath;
    }
}
