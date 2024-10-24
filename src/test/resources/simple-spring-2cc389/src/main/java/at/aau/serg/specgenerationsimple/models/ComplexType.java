package at.aau.serg.specgenerationsimple.models;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public class ComplexType {

    public ComplexType() {
    }

    @Schema(required = true)
    public List customTags;

    @NotEmpty
    public Map gradeAvgPerStudent;

    @NotNull
    public Simple someSimpleObject;

    @NotNull
    public SimpleEnum simpleEnum;

}
