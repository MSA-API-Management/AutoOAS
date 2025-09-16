package at.aau.serg.resources;

import at.aau.serg.ApiServiceImpl;
import at.aau.serg.Simple;
import at.aau.serg.interfaces.ApiService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Path("/nested-exception")
public class NestedExceptionsResource {
    ApiService apiService = new ApiServiceImpl();

    Simple simpleService = new Simple();

    @GET
    @Path("/nested-error-interface/{id}")
    public Response handleInternalServerExceptionInInterfaceImplementation(@PathParam("id") String id) {
        this.apiService.start(id);

        return Response.ok().build();
    }

    @GET
    @Path("/nested-error-concrete-class/{id}")
    public Response handleForbiddenExceptionInConcreteClassMethodCall(@PathParam("id") String id) {
        if (id.equals("error")) {
            this.simpleService.handleError();
        }

        return Response.ok().build();
    }

    @GET
    @Path("/nested-error-concrete-class-static-call/{id}")
    public Response handleNotFoundExceptionInConcreteClassStaticMethodCall(@PathParam("id") String id) {
        if (id.equals("error")) {
            Simple.handleOtherError();
        }

        return Response.ok().build();
    }

}
