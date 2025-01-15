package at.aau.serg.specgenerationsimple.models;

import java.io.Serializable;

public class Simple implements Serializable {

    public String name;

    public Integer id;

    public Simple withName(String name) {
        this.name = name;
        return this;
    }

    public Simple withId(Integer id) {
        this.id = id;
        return this;
    }

}
