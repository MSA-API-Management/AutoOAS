package at.aau.serg.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SimpleJsonProperty {
    @JsonProperty("full_name")
    public String fullName;
    @JsonProperty("user_age")
    public int age;

    public SimpleJsonProperty(String fullName,
                            int age) {
        this.fullName = fullName;
        this.age = age;
    }
}
