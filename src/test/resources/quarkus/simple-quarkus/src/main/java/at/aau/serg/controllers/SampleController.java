package at.aau.serg.controllers;

import at.aau.serg.messages.ExtendedMessage;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("/sample")
public class SampleController extends BaseController {
    @POST
    public Response addMessage(ExtendedMessage message) {
        messages.add(message);
        return Response.ok(message).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateMessage(@PathParam("id") String id,
                                  ExtendedMessage updatedMessage) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).id.equals(id)) {
                messages.set(i, updatedMessage);
                return Response.ok(updatedMessage).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteMessage(@PathParam("id") String id) {
        messages.removeIf(msg -> msg.id.equals(id));
        return Response.noContent().build();
    }

    @GET
    @Path("/response-status/{id}")
    @Operation(summary = "very interesting stuff")
    @APIResponse(responseCode = "200")
    public Response getById2(@PathParam("id") Long id) {
        return Response.status(200).build();
    }

    @GET
    @Path("/string-test")
    @Produces(MediaType.TEXT_PLAIN)
    public String getFoos(@QueryParam("id") String id) {
        return "ID: " + id;
    }

    @GET
    @Path("/{id}")
    @APIResponse(responseCode = "204")
    public Response getById(@PathParam("id") Long id) {
        return Response.ok().build();
    }
}
