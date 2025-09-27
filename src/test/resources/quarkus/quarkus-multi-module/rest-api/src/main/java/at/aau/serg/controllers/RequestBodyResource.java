package at.aau.serg.controllers;

import at.aau.serg.models.AnotherSimple;
import at.aau.serg.models.Simple;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("/rest-api-module")
public class RequestBodyResource {

    @POST
    public Response handleRequestBodyObject(AnotherSimple simple) {
        return Response.ok(simple).build();
    }

}
