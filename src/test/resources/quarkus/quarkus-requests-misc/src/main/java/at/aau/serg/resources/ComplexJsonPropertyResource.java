package at.aau.serg.resources;

import at.aau.serg.models.ComplexJsonProperty;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/complex-json")
public class ComplexJsonPropertyResource {
    @Path("/property-getter")
    @POST
    public Response handleJsonPropertyGetter(ComplexJsonProperty property) {
        return Response.ok().entity(property).build();
    }
}
