package at.aau.serg.specgenerationsimple.models;

import javax.validation.constraints.Min;

public class IgnorableSimple {

    @Min(1)
    public int id;

    public String someProp;

}
