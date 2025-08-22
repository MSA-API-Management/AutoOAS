package at.aau.serg.specgenerationsimple.models;

public class Course {

    public String name;
    public int id;

    public Course(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public Course() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
