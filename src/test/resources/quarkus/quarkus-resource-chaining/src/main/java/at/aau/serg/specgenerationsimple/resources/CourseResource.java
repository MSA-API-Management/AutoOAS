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
    @Path("/{courseId}")
    public Response getById(@PathParam("courseId") long id) {
        return Response.ok(new Course("Programming", (int)id)).build();
    }

    @GET
    public Response get() {
        return Response.ok(List.of(new Course("Programming 101", 1))).build();
    }

    @Path("{id}/students")
    public StudentResource getStudentsOfCourseById(@PathParam("id") int id) {
        if (id == 1) {
            throw new IllegalArgumentException("Invalid course id");
        } else if (id == 2) {
            throw new RuntimeException("Something went wrong");
        }

        return new StudentResource();
    }

}
