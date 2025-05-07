package at.aau.serg.specgenerationsimple.controllers;

import at.aau.serg.specgenerationsimple.models.SimpleObject;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/requests")
public class RequestsController {
    @GET
    @Path("/header-param")
    public SimpleObject headerParam(@HeaderParam("req-test") String test) {
        return new SimpleObject(test, 1);
    }

    @GET
    @Path("/header-param-required")
    public SimpleObject headerParamRequired(@HeaderParam("num") @NotNull Integer num) {
        return new SimpleObject("Test", num);
    }

    @GET
    @Path("/regex/{lastname: [a-zA-Z0-9]*}/{firstname: [A-Za-z]*}")
    public SimpleObject regexParam(
            @PathParam("lastname") @Pattern(regexp = "^[a-zA-Z0-9]*$") String lastname,
            @PathParam("firstname") @Pattern(regexp = "^[A-Za-z]*$") String firstname) {
        return new SimpleObject(lastname + " " + firstname, -1);
    }

    @GET
    @Path("/regex/{lastname}/{firstname}")
    public SimpleObject regexParamPath(
            @PathParam("lastname") @Pattern(regexp = "^[a-zA-Z0-9]*$") String lastname,
            @PathParam("firstname") @Pattern(regexp = "^[A-Za-z]*$") String firstname) {
        return new SimpleObject(lastname + " " + firstname, -1);
    }
}
