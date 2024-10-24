package at.aau.serg.parsers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import spoon.reflect.declaration.CtType;

import java.util.List;


@AllArgsConstructor
@Getter
/**
 * Contains the classes relevant for parsing.
 */
public class RelevantClasses {
    private List<CtType<?>> controllerClasses;
    private List<CtType<?>> controllerAdviceClasses;
    private List<CtType<?>> explicitModelClasses;
}
