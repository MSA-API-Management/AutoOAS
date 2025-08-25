package at.aau.serg.specgenerationsimple.models;

public class Teacher {
    public static String SOME_STATIC_FIELD = "default";

    public String name = SOME_STATIC_FIELD;
    public int id;

    public Teacher(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public Teacher() {
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
