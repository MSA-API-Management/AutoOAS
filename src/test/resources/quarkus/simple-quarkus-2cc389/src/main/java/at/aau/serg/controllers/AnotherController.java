package at.aau.serg.controllers;

import at.aau.serg.models.AnotherSimple;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.ArrayList;

@Path("/othersimples")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AnotherController {

    @GET
    public Response getAll() {
        ArrayList<AnotherSimple> al = new ArrayList<>();
        al.add(new AnotherSimple().withSsn("1324").withId(1));
        return Response.ok(al).build();
    }

    @GET
    @Path("/{id}")
    @APIResponse(responseCode = "204")
    public Response getById(@PathParam("id") Long id) {
        return Response.ok(new AnotherSimple().withSsn("2511").withId(1)).build();
    }

    @GET
    @Path("/{id}/{ssn}")
    public Response getByIdAndSsn(@PathParam("id") Long id,
                                  @PathParam("ssn") String ssn) {
        return Response.ok(new AnotherSimple().withSsn("2511").withId(1)).build();
    }

    @GET
    @Path("/string-test")
    @Produces(MediaType.TEXT_PLAIN)
    public String getFoos(@QueryParam("id") String id) {
        return "ID: " + id;
    }
}
