package at.aau.serg.specgenerationsimple.resources;

import at.aau.serg.specgenerationsimple.models.Student;
import jakarta.ws.rs.*;

import java.util.List;

public class StudentResource {

    @GET
    public List<Student> getAll() {
        return List.of(new Student("stud1", 1));
    }

}
