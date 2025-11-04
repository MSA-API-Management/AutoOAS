package at.aau.serg.specgenerationsimple.controllers;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/formparam")
public class FormParamResource {
    @POST
    public Response post(@FormParam("name") String name) {
        return Response.ok().build();
    }

    @POST
    @Path("/multiple")
    public Response postMultiple(@FormParam("name") String name, @FormParam("name2") String name2) {
        return Response.ok().build();
    }
}