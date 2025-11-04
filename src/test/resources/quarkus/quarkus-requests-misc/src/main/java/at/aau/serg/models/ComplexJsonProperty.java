package at.aau.serg.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ComplexJsonProperty {
    private final String id;
    private final String value;
    private String name;

    @JsonCreator
    public ComplexJsonProperty(@JsonProperty("json.id") String id,
                               @JsonProperty("json.value") String value) {
        this.id = id;
        this.value = value;
    }

    @JsonProperty
    public String getId() {
        return id;
    }

    @JsonProperty("json.name")
    public String getName() {
        return name;
    }

    @JsonProperty("json.value")
    public String getValue() {
        return value;
    }


}
