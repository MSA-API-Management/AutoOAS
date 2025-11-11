package at.aau.serg.resources;

import at.aau.serg.models.OptionalSimple;
import at.aau.serg.models.Simple;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.util.Optional;

@Path("/optionals")
public class OptionalRequestsResource {

    @POST
    @Path("/in-request/string")
    public Response postString(Optional<String> s) {
        return Response.noContent().build();
    }

    @POST
    @Path("/in-request/primitive")
    public Response postInt(Optional<Integer> i) {
        return Response.noContent().build();
    }

    @POST
    @Path("/in-request/obj")
    public Response postObj(Optional<Simple> s) {
        return Response.noContent().build();
    }

    @POST
    @Path("/in-request/obj-with-optional-field")
    public Response postObj(OptionalSimple os) {
        return Response.noContent().build();
    }

    @GET
    @Path("/in-response/obj")
    public Optional<Simple> getOptionalSimple() {
        return Optional.empty();
    }

    @GET
    @Path("/in-response/response-with-obj")
    public Response getResponseWithOptionalSimple() {
        return Response.ok(Optional.empty()).build();
    }

}

