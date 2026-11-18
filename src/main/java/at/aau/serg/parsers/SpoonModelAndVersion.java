package at.aau.serg.parsers;

import lombok.AllArgsConstructor;
import lombok.Data;
import spoon.reflect.CtModel;

@Data
@AllArgsConstructor
public class SpoonModelAndVersion {

    /**
     * The Spoon model.
     */
    private CtModel model;

    /**
     * The developer-defined project version in the pom.
     */
    private String projectVersion;

}
