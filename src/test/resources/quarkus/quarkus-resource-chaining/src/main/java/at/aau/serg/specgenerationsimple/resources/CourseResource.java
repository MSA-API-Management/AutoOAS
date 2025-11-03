package at.aau.serg.specgenerationsimple.resources;

import at.aau.serg.specgenerationsimple.models.Course;
import at.aau.serg.specgenerationsimple.models.Teacher;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.util.List;

public class CourseResource extends AbstractResource {

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        return Response.ok(new Course("Programming", id)).build();
    }

    @GET
    public Response get() {
        return Response.ok(List.of(new Course("Programming 101", 1))).build();
    }

    @Path("{id}/students")
    public StudentResource getStudentsOfCourseById(@PathParam("id") int id) {
        return new StudentResource();
    }

}
