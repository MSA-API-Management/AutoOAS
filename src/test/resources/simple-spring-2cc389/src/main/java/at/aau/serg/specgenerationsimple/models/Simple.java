package at.aau.serg.specgenerationsimple.models;

import java.io.Serializable;

public class Simple extends SimpleBase implements Serializable {

    public String name;

    public Integer id;

    public double avgGrade;

    public Simple withName(String name) {
        this.name = name;
        return this;
    }

    public Simple withId(Integer id) {
        this.id = id;
        return this;
    }

    public Simple withAvgGrade(double avgGrade) {
        this.avgGrade = avgGrade;
        return this;
    }

}
