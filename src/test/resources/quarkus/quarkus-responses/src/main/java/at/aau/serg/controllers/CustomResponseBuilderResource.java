package at.aau.serg.controllers;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/custom-response-builder")
public class CustomResponseBuilderResource {

    @GET
    @Path("/accept")
    public Object getResponse() {
        return buildResponse(Response.Status.ACCEPTED);
    }

    @GET
    @Path("/echo")
    public Response getEchoResponse(Object request) {
        return buildResponse(Response.Status.OK, request);
    }

    @GET
    @Path("/random")
    public Response getRandomResponse(Object request) {
        if (Math.random() < 0.5) {
            return buildResponse(Response.Status.BAD_REQUEST);
        } else if (Math.random() < 0.5) {
            return buildResponse(Response.Status.FORBIDDEN);
        } else {
            return buildResponse(Response.Status.OK, request);
        }
    }

    private Response buildResponse(Response.Status status) {
        return buildResponse(status, null);
    }

    private <T> Response buildResponse(Response.Status status, T content) {
        return Response.status(status).entity(content).build();
    }

}
