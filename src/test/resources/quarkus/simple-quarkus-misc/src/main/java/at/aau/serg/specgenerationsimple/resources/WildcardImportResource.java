package at.aau.serg.specgenerationsimple.resources;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("/wildcard")
public class WildcardImportResource {
    @GET
    @Path("/get-card")
    public Response getWildcard() {
        return Response.ok().build();
    }

    @POST
    @Path("/post-card")
    public Response postWildcard() {
        return Response.ok().build();
    }
}
