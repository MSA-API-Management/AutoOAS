package at.aau.serg.models;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.QueryParam;

import java.io.Serializable;

public class ComplexBean implements Serializable {
    @FormParam("id")
    public String id;

    @QueryParam("name")
    public String name;

    @HeaderParam("address")
    public String address;
}
