package at.aau.serg.specgenerationsimple.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/api")
public class BaseResource {

    @GET
    @Path("/alive")
    public Response getAlive() {
        return Response.accepted().build();
    }

    @Path("/teachers")
    public TeacherResource getTeacherResource() {
        return new TeacherResource();
    }

    @Path("/students")
    public StudentResource getStudentResource() {
        return new StudentResource();
    }

}
