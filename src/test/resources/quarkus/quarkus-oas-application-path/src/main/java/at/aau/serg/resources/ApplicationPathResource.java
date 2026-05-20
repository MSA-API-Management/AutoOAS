package at.aau.serg.resources;


import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/api")
public class ApplicationPathResource {
    @GET
    @Path("/items")
    public Response getItems() {
        return Response.ok().build();
    }

    @POST
    @Path("/items")
    public Response createItem(String item) {
        return Response.status(Response.Status.CREATED).build();
    }
}