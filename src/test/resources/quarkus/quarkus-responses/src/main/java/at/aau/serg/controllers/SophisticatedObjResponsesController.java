package at.aau.serg.controllers;

import at.aau.serg.models.Simple;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/sophisticated-simple-responses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SophisticatedObjResponsesController {

    @GET
    @Path("/some-success-status-code")
    public Response getSomeSuccessStatusCode() {
        return Response.noContent().build();
    }

    @GET
    @Path("/object-inline-status-method")
    public Response getSimpleWithStatusMethod() {
        return Response.status(Response.Status.OK).entity(new Simple().withName("Peter").withId(1)).build();
    }

    @GET
    @Path("/status-method-error")
    public Response getErrorStatusMethod() {
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @GET
    @Path("/status-method-with-response-obj")
    public Response getErrorStatusMethodWithResponse() {
        return Response.status(Response.Status.UNAUTHORIZED).entity(new Simple().withName("Peter").withId(1)).build();
    }

    // fixme not detecting the type, only status code
    @GET
    @Path("/nested-builder")
    public Response getNestedBuilder() {
        var val = new Simple().withName("Peter").withId(1);
        Response.ResponseBuilder builder = Response.ok(val);
        return builder.status(Response.Status.CREATED).build(); // 201
    }

    @GET
    @Path("/numeric-status-code")
    public Response getWithNumericStatus() {
        var val = new Simple().withName("Peter").withId(1);
        return Response.status(418).entity(val).build(); // 418 I'm a teapot
    }

    // fixme not working !!
    @GET
    @Path("/prev-assigned-var-response")
    public Response getResponseFromVar() {
        var val = new Simple().withName("Peter").withId(1);
        Response response = Response.ok(val).build();
        return response; // response built before return
    }

    @GET
    @Path("/various-responses")
    public Response getVariousResponses() {
        if (Math.random() > 0.5) {
            return Response.status(201).entity(new Simple().withName("Peter").withId(1)).build();
        } else if (Math.random() > 0.5) {
            return Response.noContent().build();
        } else {
            return Response.ok("Worked").build();
        }
    }

}
