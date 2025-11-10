package at.aau.serg.parsers;

import lombok.Getter;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private List<CtType<?>> parentResourceTypeChain;

    /**
     * The path prefix defined for the controller.
     */
    private String basePath;

    public ControllerClassProcessingInformation(CtType<?> concreteControllerType, CtType<?> currentSuperclassType) {
        this(concreteControllerType, currentSuperclassType, "");
    }

    public ControllerClassProcessingInformation(CtType<?> concreteControllerType, CtType<?> currentSuperclassType, String basePath) {
        this(concreteControllerType, currentSuperclassType, List.of(), basePath);
    }

    public ControllerClassProcessingInformation(CtType<?> concreteControllerType, CtType<?> currentSuperclassType, List<CtType<?>> parentResourceTypeChain, CtType<?> nextResourceTypeInChain, String basePath) {
        this(concreteControllerType, currentSuperclassType, parentResourceTypeChain, basePath);

        this.parentResourceTypeChain.add(nextResourceTypeInChain);
    }

    public ControllerClassProcessingInformation(CtType<?> concreteControllerType, CtType<?> currentSuperclassType, List<CtType<?>> parentResourceTypeChain, String basePath) {
        this.concreteControllerType = concreteControllerType;
        this.currentSuperclassType = currentSuperclassType;
        this.parentResourceTypeChain = new ArrayList<>(parentResourceTypeChain);
        this.basePath = basePath;
    }

    public String getSubResourceChainAsString() {
        return parentResourceTypeChain.stream().map(ctType -> ctType.getQualifiedName() + " - ").collect(Collectors.joining());
    }

    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ControllerClassProcessingInformation that)) return false;
        return Objects.equals(concreteControllerType, that.concreteControllerType)
                && Objects.equals(currentSuperclassType, that.currentSuperclassType)
                && parentResourceTypeChain.equals(that.parentResourceTypeChain)
                && Objects.equals(basePath, that.basePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(concreteControllerType, currentSuperclassType, parentResourceTypeChain, basePath);
    }

    @Override
    public String toString() {
        return "ControllerClassProcessingInformation{" +
                "concreteControllerType=" + concreteControllerType.getQualifiedName() +
                ", currentSuperclassType=" + currentSuperclassType.getQualifiedName() +
                ", currentParentResourceType=" + getSubResourceChainAsString() +
                ", basePath='" + basePath + '\'' +
                '}';
    }
}
