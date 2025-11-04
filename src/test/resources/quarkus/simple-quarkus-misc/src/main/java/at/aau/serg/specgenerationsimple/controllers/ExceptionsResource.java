package at.aau.serg.specgenerationsimple.controllers;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("")
public class ExceptionsResource {
    @GET
    @Path("/potentially-unsupported")
    public String potentiallyUnsupported() {
        if (Math.random() < 0.5) {
            throw new UnsupportedOperationException("unsupported");
        }
        return "unsupported";
    }

    @GET
    @Path("/unsupported")
    public String unsupported() {
        throw new UnsupportedOperationException("unsupported");
    }
}
