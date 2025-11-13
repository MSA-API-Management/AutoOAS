package at.aau.serg.frameworks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

@AllArgsConstructor
@Getter
public class SubResource {

    private CtType<?> type;

    private String path;

    private CtMethod<?> servingMethod;

}
