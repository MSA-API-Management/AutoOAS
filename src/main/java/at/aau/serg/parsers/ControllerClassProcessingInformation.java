package at.aau.serg.parsers;

import lombok.Getter;
import spoon.reflect.declaration.CtType;

import java.util.Objects;

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
     * The type of the class providing this controller used during processing, e.g., PR::getC -> R
     */
    private CtType<?> currentParentResourceType;

    /**
     * The path prefix defined for the controller.
     */
    private String basePath;

    public ControllerClassProcessingInformation(CtType<?> concreteControllerType, CtType<?> currentSuperclassType) {
        this(concreteControllerType, currentSuperclassType, "");
    }

    public ControllerClassProcessingInformation(CtType<?> concreteControllerType, CtType<?> currentSuperclassType, String basePath) {
        this(concreteControllerType, currentSuperclassType, null, basePath);
    }

    public ControllerClassProcessingInformation(CtType<?> concreteControllerType, CtType<?> currentSuperclassType, CtType<?> currentParentResourceType, String basePath) {
        this.concreteControllerType = concreteControllerType;
        this.currentSuperclassType = currentSuperclassType;
        this.currentParentResourceType = currentParentResourceType;
        this.basePath = basePath;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ControllerClassProcessingInformation that)) return false;
        return Objects.equals(concreteControllerType, that.concreteControllerType)
                && Objects.equals(currentSuperclassType, that.currentSuperclassType)
                && Objects.equals(currentParentResourceType, that.currentParentResourceType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(concreteControllerType, currentSuperclassType, currentParentResourceType);
    }
}
