package at.aau.serg.specgenerationsimple.resources;

import jakarta.ws.rs.Path;

@Path("/api")
public class BaseResource {

    @Path("/teachers")
    public TeacherResource getTeacherResource() {
        return new TeacherResource();
    }

    @Path("/students")
    public StudentResource getStudentResource() {
        return new StudentResource();
    }

}
