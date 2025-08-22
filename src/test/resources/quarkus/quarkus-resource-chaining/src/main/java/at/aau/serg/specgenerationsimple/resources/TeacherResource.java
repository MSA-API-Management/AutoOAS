package at.aau.serg.specgenerationsimple.resources;

import at.aau.serg.specgenerationsimple.models.Teacher;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

public class TeacherResource {

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        return Response.ok(new Teacher("Doe", id)).build();
    }

}
