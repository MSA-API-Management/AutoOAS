package at.aau.serg.specgenerationsimple.resources;

import at.aau.serg.specgenerationsimple.models.LocalTimeOrInstant;
import at.aau.serg.specgenerationsimple.models.SimpleObject;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("/requests")
public class RequestsResource {
    @GET
    @Path("/header-param")
    public SimpleObject headerParam(@HeaderParam("req-test") String num) {
        return new SimpleObject(num, 1);
    }

    @GET
    @Path("/header-param-required-not-null")
    public SimpleObject headerParamRequiredNotNull(@HeaderParam("num") @NotNull String num) {
        return new SimpleObject("Test", 1);
    }

    @GET
    @Path("/query-param")
    public SimpleObject queryParam(@QueryParam("req-test") String num) {
        return new SimpleObject(num, 1);
    }

    @GET
    @Path("/query-param-required-not-null")
    public SimpleObject queryParamRequiredNotNull(@QueryParam("num") @NotNull String num) {
        return new SimpleObject("Test", 1);
    }

    @GET
    @Path("/local-time-or-instant")
    public Response queryParamLocalTimeOrInstantObject(@QueryParam("session_time") LocalTimeOrInstant localTime) {
        return Response.ok().build();
    }

    @GET
    @Path("/regex-url/{lastname:^[a-zA-Z0-9]*$}/{firstname:^[A-Za-z]*$}")
    public SimpleObject regexParam(
            @PathParam("lastname") @Pattern(regexp = "^[a-zA-Z0-9]*$") String lastname,
            @PathParam("firstname") @Pattern(regexp = "^[A-Za-z]*$") String firstname) {
        return new SimpleObject(lastname + " " + firstname, -1);
    }

    @GET
    @Path("/regex-validation/{lastname}/{firstname}")
    public SimpleObject regexParamPath(
            @PathParam("lastname") @Pattern(regexp = "^[a-zA-Z0-9]*$") String lastname,
            @PathParam("firstname") @Pattern(regexp = "^[A-Za-z]*$") String firstname) {
        return new SimpleObject(lastname + " " + firstname, -1);
    }
}
