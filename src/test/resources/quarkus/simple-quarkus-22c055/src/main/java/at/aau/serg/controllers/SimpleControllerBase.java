package at.aau.serg.controllers;

import at.aau.serg.models.Simple;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class SimpleControllerBase {
    @GET
    @Path("/controller-superclass-endpoint")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSuperclass() {
        return Response.ok(new Simple().withId(42)).build();
    }

    @PATCH
    @Path("/controller-superclass-endpoint")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSuperclassPatch() {
        return Response.ok(new Simple().withId(42)).build();
    }
}
