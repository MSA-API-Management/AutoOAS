package at.aau.serg.models;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;
import java.util.Map;

public class ComplexType {

    @Schema(required = true)
    public List customTags;
    @NotEmpty
    public Map gradeAvgPerStudent;
    @NotNull
    public Simple someSimpleObject;
    @NotNull
    public SimpleEnum simpleEnum;

    public ComplexType() {
    }
}
