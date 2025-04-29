package at.aau.serg.models;

import jakarta.validation.constraints.Min;

public class IgnorableSimple {
    @Min(1)
    public int id;

    public String someProp;

}
