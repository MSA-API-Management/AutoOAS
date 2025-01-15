package at.aau.serg.specgenerationsimple.models;

public class SimpleObject {
    public static String SOME_STATIC_FIELD = "default";

    public String name = SOME_STATIC_FIELD;
    public int age;

    public SimpleObject(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public SimpleObject() {}

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
