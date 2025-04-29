package at.aau.serg.models;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class AnotherSimple {
    @NotNull
    public String notNullField;

    @Deprecated
    public String someDeprecatedField;

    @Schema(readOnly = true, description = "social security number")
    public String ssn;

    @DecimalMin("0.0")
    @DecimalMax("0.0")
    public double avgGrade;

    @Min(1)
    public int id;

    public AnotherSimple withSsn(String ssn) {
        this.ssn = ssn;
        return this;
    }

    public AnotherSimple withAvgGrade(double avgGrade) {
        this.avgGrade = avgGrade;
        return this;
    }

    public AnotherSimple withId(int id) {
        this.id = id;
        return this;
    }
}
