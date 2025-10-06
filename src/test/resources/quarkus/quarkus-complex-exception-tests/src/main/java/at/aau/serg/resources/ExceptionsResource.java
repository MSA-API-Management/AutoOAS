package at.aau.serg.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Path("/exception")
public class ExceptionsResource {
    @GET
    @Path("/simple-error/{id}")
    public Response handleRuntimeExceptionInMethod(@PathParam("id") String id) {
        if (id.equals("error")) {
            throw new RuntimeException("Unexpected error");
        }
        return Response.ok().build();
    }

    @GET
    @Path("/method-error/{id}")
    public Response handleRuntimeExceptionInCalledMethod(@PathParam("id") String id) {
        if (id.equals("error")) {
            handleError();
        }
        return Response.ok().build();
    }

    @GET
    @Path("/method-error-specific-response-code/{id}")
    public Response handleNotFoundEceptionInMethodCall(@PathParam("id") String id) {
        if (id.equals("error")) {
            handleNotFoundError();
        }
        return Response.ok().build();
    }

    private void handleError() {
        throw new RuntimeException("Unexpected error");
    }

    private void handleNotFoundError() {
        throw new NotFoundException("Not found");
    }
}
