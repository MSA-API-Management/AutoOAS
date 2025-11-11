package at.aau.serg.resources;

import at.aau.serg.models.SimpleValidation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path("/validation")
public class ValidationTestResource {
    @POST
    @Path("/with-valid")
    public Response handleRequestBodyObjectWithValidAnnotation(@Valid SimpleValidation simple) {
        return Response.ok(simple).build();
    }

    @POST
    @Path("/without-valid")
    public Response handleRequestBodyObjectWithoutValidAnnotation(SimpleValidation simple) {
        return Response.ok(simple).build();
    }

    @POST
    @Path("/notnull-valid")
    public Response handleRequestBodyObjectWithValidAndNotNullParamAnnotation(@NotNull @Valid SimpleValidation simple) {
        return Response.ok(simple).build();
    }

    @POST
    @Path("/notnull-only")
    public Response handleRequestBodyObjectWithoutValidButNotNullParamAnnotation(@NotNull SimpleValidation simple) {
        return Response.ok(simple).build();
    }

    @POST
    @Path("/validated-query")
    public Response handleRequestBodyObjectWithValidAndNotNullParamAnnotationAndQueryParamWithMinAnnotation(@NotNull @Valid SimpleValidation simple,
                                                                                                            @Min(1) @QueryParam("version") int version) {
        return Response.ok(simple).build();
    }

    @POST
    @Path("/notnull-query")
    public Response handleRequestBodyObjectWithoutValidButNotNullParamAnnotationAndQueryParamWithMinAnnotation(@NotNull SimpleValidation simple,
                                                                                                               @Min(1) @QueryParam("version") int version) {
        return Response.ok(simple).build();
    }

}
