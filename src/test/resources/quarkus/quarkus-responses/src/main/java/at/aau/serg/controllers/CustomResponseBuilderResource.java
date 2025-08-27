package at.aau.serg.controllers;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/custom-response-builder")
public class CustomResponseBuilderResource {

    @GET
    @Path("/accept")
    public Object getResponse() {
        return buildResponse(Response.Status.ACCEPTED);
    }

    @POST
    @Path("/echo")
    public Response getEchoResponse(String request) {
        return buildResponse(Response.Status.OK, request);
    }

    @POST
    @Path("/random")
    public Object getRandomResponse(String request) {
        if (Math.random() < 0.5) {
            return buildResponse(Response.Status.BAD_REQUEST);
        } else if (Math.random() < 0.5) {
            return buildResponse(Response.Status.FORBIDDEN);
        } else if (Math.random() < 0.5) {
            return buildResponse(Response.Status.CREATED, request);
        } else {
            return request;
        }
    }

    private Response buildResponse(Response.Status status) {
        return buildResponse(status, null);
    }

    private <T> Response buildResponse(Response.Status status, T content) {
        return Response.status(status).entity(content).build();
    }

}
