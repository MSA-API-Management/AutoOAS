package at.aau.serg.controllers;

import at.aau.serg.models.SimpleJsonProperty;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/json-property")
public class JsonPropertyResource {
    @POST
    public Response handleSimpleJsonPropertyRequestBody(SimpleJsonProperty request) {
        return Response.ok(request).build();
    }

    @GET
    public Response handleSimpleJsonPropertyReturnValue() {
        SimpleJsonProperty property = new SimpleJsonProperty("Name", 1);
        return Response.ok(property).build();
    }
}
