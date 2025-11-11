package at.aau.serg.models;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class GetterConstraints {
    @NotNull
    @Size(min = 1)
    private String type;

    @NotNull
    @Size(max = 1)
    public String otherType;

    public GetterConstraints() {
        this.type = "1";
        this.otherType = "2";
    }

    public String getType() {
        return type;
    }
}
