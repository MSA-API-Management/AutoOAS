package at.aau.serg.controllers;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/exceptions")
public class AnotherSimpleController {
    @POST
    @Path("/throw")
    public Response handleJakartaBadRequestAndJavaGeneralException(String body) {
        if (body.equals("testing")) {
            throw new BadRequestException();
        } else if (body.equals("testing2")) {
            throw new RuntimeException();
        }

        return Response.status(Response.Status.ACCEPTED).build();
    }

    @POST
    @Path("/another-throw")
    public Response handleDifferentJakartaExceptions(String body) {
        if (body.equals("testing")) {
            throw new ForbiddenException();
        } else if (body.equals("testing2")) {
            return Response.serverError().build();
        }
        return Response.status(200).build();
    }

    @POST
    @Path("/complex-throw")
    public Response handleComplexJakartaThrowInSubMethod(String test) {
        if (test.equals("testing")) {
            throw this.newBadRequestException("Bad");
        } else if (test.equals("testing2")) {
            throw new ForbiddenException();
        }
        throw new RuntimeException();
    }

    private BadRequestException newBadRequestException(String errorMessage) {
        Response.ResponseBuilder builder = Response.status(404);
        builder.type(APPLICATION_JSON);
        return new BadRequestException(builder.build());
    }
}
