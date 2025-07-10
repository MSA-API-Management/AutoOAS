package at.aau.serg.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class ComplexJsonProperty {
    @JsonProperty("full_name")
    @NotNull
    public String fullName;
    @JsonProperty("user_age")
    public int age;

    @JsonProperty("int_value")
    @NotNull
    public int value;

    public ComplexJsonProperty(String fullName,
                               int age,
                               int value) {
        this.fullName = fullName;
        this.age = age;
        this.value = value;
    }
}
