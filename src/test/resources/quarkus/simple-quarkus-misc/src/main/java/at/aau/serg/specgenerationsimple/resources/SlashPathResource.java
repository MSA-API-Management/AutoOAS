package at.aau.serg.specgenerationsimple.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/")
public class SlashPathResource {
    @GET
    public Response handleSlashClassPathAndNoSpecifiedMethodPath() {
        return Response.ok().build();
    }

    @POST
    @Path("")
    public Response handleSlashClassPathAndEmptyMethodPath() {
        return Response.ok().build();
    }

    @PUT
    @Path("/")
    public Response handleSlashClassPathAndSlashMethodPath() {
        return Response.ok().build();
    }

    @GET
    @Path("/value")
    public Response handleSlashClassPathAndSpecifiedMethodPath() {
        return Response.ok().build();
    }
}
