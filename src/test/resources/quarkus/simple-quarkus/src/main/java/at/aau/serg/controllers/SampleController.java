package at.aau.serg.controllers;

import at.aau.serg.messages.ExtendedMessage;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

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
}
