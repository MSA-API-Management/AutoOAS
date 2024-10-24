package at.aau.serg.specgenerationsimple.models;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public class ComplexType {

    public ComplexType() {
    }

    @NotNull
    public List customTags;

    @NotNull
    public Map gradeAvgPerStudent;

    @NotNull
    public Simple someSimpleObject;

    @NotNull
    public SimpleEnum simpleEnum;

}
