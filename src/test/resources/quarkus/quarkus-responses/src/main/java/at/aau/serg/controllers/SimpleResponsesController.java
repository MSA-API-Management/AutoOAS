package at.aau.serg.controllers;

import at.aau.serg.models.Simple;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

@Path("/simple-responses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SimpleResponsesController {

    @GET
    @Path("/array-multiple-lines")
    public Response getListOfSimple() {
        ArrayList<Simple> al = new ArrayList<>();
        al.add(new Simple().withName("Peter").withId(1));
        return Response.ok(al).build();
    }

    @GET
    @Path("/array-inline")
    public Response getListOfSimplesInline() {
        return Response.ok(List.of(new Simple().withName("Peter").withId(1))).build();
    }

    @GET
    @Path("/object-multiple-lines")
    public Response getSimple() {
        var val = new Simple().withName("Peter").withId(1);
        return Response.ok(val).build();
    }

    @GET
    @Path("/object-inline")
    public Response getSimpleInline() {
        return Response.ok(new Simple().withName("Peter").withId(1)).build();
    }

    @GET
    @Path("/primitive-echo")
    public Response getStringEcho(@QueryParam("name") String name) {
        return Response.ok(name).build();
    }


}
