package at.aau.serg.controllers;

import at.aau.serg.models.DtoWithGetters;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("/dto-with-getters")
public class RequestBodyGetterMethodsResource {
    @PUT
    @Path("/dto")
    public Response put(DtoWithGetters dto) {
        System.out.println(dto);
        return Response.noContent().build();
    }
}
