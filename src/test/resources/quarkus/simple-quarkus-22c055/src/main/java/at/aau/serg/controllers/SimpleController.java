package at.aau.serg.controllers;

import at.aau.serg.models.Simple;
import at.aau.serg.models.SimpleEnum;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Equivalent to simple-spring-22c055
@Path("/simples")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SimpleController {

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

    @GET
    @Path("/void-response")
    public void getAllNoReturn() {
        ArrayList<Simple> al = new ArrayList<>();
        al.add(new Simple().withName("Peter").withId(1));
    }

    @GET
    @Path("/raw-list")
    public Response getAllRawList() {
        ArrayList<Simple> al = new ArrayList<>();
        al.add(new Simple().withName("Peter").withId(1));
        return Response.ok(al).build();
    }

    @GET
    @Path("/enum")
    public Response getEnum() {
        return Response.ok(SimpleEnum.NICE).build();
    }

    @GET
    @Path("/simple-superclass-property")
    public Response getSuperclassSimple() {
        Simple s = new Simple();
        s.id = 50;
        s.name = "I contain superclass field values";
        return Response.ok(s).build();
    }

    @POST
    @Path("request-body-list")
    public Response create(List<Simple> simples) {
        return Response.ok(simples).build();
    }

    @POST
    @Path("request-body-object")
    public Response create(Simple simple) {
        return Response.ok(simple).build();
    }

    @POST
    @Path("unique-operation-ids")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response create(String str) {
        return Response.ok().entity(null).build();
    }

    @DELETE
    @Path("/{id}")
    public Map<String, Boolean> delete(@PathParam("id") Long id) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return response;
    }
}
