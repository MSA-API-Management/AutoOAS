package at.aau.serg.controllers;

import at.aau.serg.models.SimpleEnum;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("/wrapped-primitive-responses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WrappedPrimitiveResponsesController {

    @GET
    @Path("/int")
    public Response getInt() {
        return Response.ok(5).build();
    }

    @GET
    @Path("/str")
    public Response getString() {
        return Response.ok("hello").build();
    }

    @GET
    @Path("/double")
    public Response getDouble() {
        return Response.ok(5.1).build();
    }

    @GET
    @Path("/bool")
    public Response getBool() {
        return Response.ok(true).build();
    }

    @GET
    @Path("/simple-enum")
    public Response getEnum() {
        return Response.ok(SimpleEnum.NICE).build();
    }

    @GET
    @Path("/primitive-echo")
    public Response getStringEcho(@QueryParam("name") String name) {
        return Response.ok(name).build();
    }


}
