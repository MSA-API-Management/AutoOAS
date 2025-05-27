package at.aau.serg.models;

import jakarta.ws.rs.FormParam;

import java.io.Serializable;

public class SimpleBean implements Serializable {
    @FormParam("name")
    public String name;
}
