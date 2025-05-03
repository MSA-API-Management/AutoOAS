package at.aau.serg.controllers;

import at.aau.serg.exceptions.CustomRuntimeException;
import at.aau.serg.models.SimpleObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/simple")
@Produces(MediaType.APPLICATION_JSON)
public class SimpleController {
    @GET
    @Path("/{id}")
    public Response should_throwCustomException_when_idIsZeroOrNegative_else_returnResource(@PathParam("id") int id) {
        if (id <= 0) {
            throw new CustomRuntimeException("Resource with id " + id + " not found");
        }

        return Response.ok(new SimpleObject("Item " + id, id)).build();
    }

    @GET
    @Path("/validation/{id}")
    public Response should_throwIllegalArgumentOrUnsupportedOperation_when_idIsNegativeOrZero_else_returnResource(@PathParam("id") int id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID cannot be negative");
        }
        if (id == 0) {
            throw new UnsupportedOperationException("Zero ID not supported");
        }

        return Response.ok(new SimpleObject("Validated " + id, id)).build();
    }
}
