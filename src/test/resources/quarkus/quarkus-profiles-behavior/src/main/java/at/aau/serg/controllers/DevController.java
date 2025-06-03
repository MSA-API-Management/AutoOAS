package at.aau.serg.controllers;

import at.aau.serg.models.Simple;
import at.aau.serg.models.SimpleEnum;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IfBuildProfile("dev")
@Path("/dev")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DevController {

    @GET
    public Response getAll() {
        ArrayList<Simple> al = new ArrayList<>();
        al.add(new Simple().withName("Peter").withId(1));
        return Response.ok(al).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        if (id < 1)
            return Response.status(Response.Status.NOT_FOUND).entity(new Simple()).build();

        return Response.ok(new Simple().withName("Peter").withId(1)).build();
    }

    @GET
    @Path("/response-status/{id}")
    public Response getById2(@PathParam("id") Long id) {
        return Response.status(404).entity(new Simple().withName("Peter").withId(1)).build();
    }

    @GET
    @Path("/primitive-list")
    public Response getAllPrimitiveList() {
        ArrayList<Integer> al = new ArrayList<>();
        al.add(7);
        return Response.ok(al).build();
    }

}
