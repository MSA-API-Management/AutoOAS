package at.aau.serg.specgenerationsimple.models;

import com.fasterxml.jackson.annotation.JsonProperty;

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
