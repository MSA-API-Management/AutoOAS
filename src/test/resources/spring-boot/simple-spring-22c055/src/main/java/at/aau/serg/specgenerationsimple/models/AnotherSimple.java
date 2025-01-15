package at.aau.serg.specgenerationsimple.models;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.*;

public class AnotherSimple {

    @NotNull
    public String notNullField;

    @Deprecated
    public String someDeprecatedField;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "social security number")
    public String ssn;

    @DecimalMin("0.0")
    @DecimalMax("10.0")
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
