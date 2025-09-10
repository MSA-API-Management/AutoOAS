package at.aau.serg.controllers;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

import java.time.Instant;

@Path("/sse")
public class ServerSentEventResponseResource {
    @GET
    @Path("/single")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void singleEvent(@Context Sse sse,
                            @Context SseEventSink sink) {
        OutboundSseEvent event = sse.newEvent("single", "Hello SSE at " + Instant.now());
        sink.send(event);
        sink.close();
    }

    @GET
    @Path("/multi")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void multiEvents(@Context Sse sse,
                            @Context SseEventSink sink) {
        try (sink) {
            for (int i = 1; i <= 3; i++) {
                OutboundSseEvent event = sse.newEvent("multi", "Event #" + i + " at " + Instant.now());
                sink.send(event);
                Thread.sleep(3000);
            }
        } catch (InterruptedException ignored) {
        }
    }

    @GET
    @Path("/infinite")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void infiniteStream(@Context Sse sse,
                               @Context SseEventSink sink) {
        new Thread(() -> {
            int counter = 0;
            try {
                while (!sink.isClosed()) {
                    OutboundSseEvent event = sse.newEvent("infinite", "Event #" + counter++);
                    sink.send(event);
                    Thread.sleep(2000);
                }
            } catch (InterruptedException e) {
                System.out.println("Interrupted");
            }
        }).start();
    }
}
