package at.aau.serg.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public class SimpleValidation implements Serializable {

    @NotNull
    public String id;
    @NotNull
    public int age;
    @Min(1)
    public int value;

    public SimpleValidation(String id,
                            int age,
                            int value) {
        this.id = id;
        this.age = age;
        this.value = value;
    }
}
