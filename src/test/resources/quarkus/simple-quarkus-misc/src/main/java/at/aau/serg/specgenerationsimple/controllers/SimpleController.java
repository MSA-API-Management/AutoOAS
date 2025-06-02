package at.aau.serg.specgenerationsimple.controllers;

import at.aau.serg.specgenerationsimple.models.SimpleObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Path("/simples")
@Produces(MediaType.APPLICATION_JSON)
public class SimpleController {
    @GET
    @Path("/deferred-empty")
    public CompletionStage<SimpleObject> getAsyncResult() {
        return CompletableFuture.completedFuture(new SimpleObject("test", 1));
    }

    @GET
    @Path("/deferred-questionmark")
    public CompletionStage<?> getAsyncResultGenericCapture() {
        return CompletableFuture.completedFuture(new SimpleObject("test", 1));
    }

    @GET
    @Path("/deferred-simple-object")
    public CompletionStage<SimpleObject> getAsyncResultStringCapture() {
        return CompletableFuture.completedFuture(new SimpleObject("test", 1));
    }

    @GET
    @Path("/deferred-response-entity-simple-object")
    public CompletionStage<Response> getAsyncResultResponseEntityStringCapture() {
        return CompletableFuture.completedFuture(
                Response.ok(new SimpleObject("test", 1)).build()
        );
    }
}
