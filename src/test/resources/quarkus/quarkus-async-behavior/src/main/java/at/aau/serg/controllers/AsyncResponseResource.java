package at.aau.serg.controllers;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Path("/async-response")
public class AsyncResponseResource {
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    @GET
    @Path("/completed-future")
    @Produces(MediaType.APPLICATION_JSON)
    public void completedFutureResponse(@Suspended AsyncResponse asyncResponse) {
        CompletableFuture<List<String>> response = CompletableFuture.completedFuture(Arrays.asList("topic1"));
        asyncResponse.resume(Response.ok(response.join()).build());
    }

    @GET
    @Path("/delayed")
    public void delayedResponse(@Suspended AsyncResponse asyncResponse) {
        CompletableFuture.supplyAsync(() -> {
            sleep(3000);
            return "Response sent at: " + Instant.now();
        }, executor).thenAccept(result -> {
            System.out.println("Resuming response for /delayed");
            asyncResponse.resume(result);
        });
    }

    @GET
    @Path("/delayed-with-code")
    public void delayedResponseWithStatusCode(@Suspended AsyncResponse asyncResponse) {
        CompletableFuture.supplyAsync(() -> {
            sleep(3000);
            return "Response sent at: " + Instant.now();
        }, executor).thenAccept(result -> {
            System.out.println("Resuming response for /delayed");
            asyncResponse.resume(Response.accepted().build());
        });
    }

    @GET
    @Path("/immediate")
    public String immediateResponse() {
        System.out.println("Immediate response");
        return "Immediate response at: " + Instant.now();
    }

    @GET
    @Path("/error")
    public void errorResponse(@Suspended AsyncResponse asyncResponse) {
        CompletableFuture.runAsync(() -> {
            sleep(2000);
            throw new RuntimeException("Error");
        }, executor).whenComplete((res, ex) -> {
            if (ex != null) {
                System.out.println("Resuming with error for /error");
                asyncResponse.resume(ex);
            } else {
                asyncResponse.resume("Should not happen");
            }
        });
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
}
