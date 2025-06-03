package at.aau.serg.models;

import java.io.Serializable;

public class Simple implements Serializable {
    public String id;
    public String name;
    public String lastname;
    public String address;

    public Simple(String id,
                  String name,
                  String lastname,
                  String address) {
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.address = address;
    }
}