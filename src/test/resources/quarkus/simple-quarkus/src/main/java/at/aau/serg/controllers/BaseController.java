package at.aau.serg.controllers;

import at.aau.serg.messages.BaseMessage;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

@Path("/base")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BaseController {
    protected final List<BaseMessage> messages = new ArrayList<>();

    @GET
    public Response getMessages() {
        return Response.ok(messages).build();
    }
}
