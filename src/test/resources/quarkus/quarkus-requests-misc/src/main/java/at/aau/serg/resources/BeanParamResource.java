package at.aau.serg.resources;

import at.aau.serg.models.ComplexBean;
import at.aau.serg.models.Simple;
import at.aau.serg.models.SimpleBean;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("/bean")
public class BeanParamResource {
    @POST
    @Path("/simple-object")
    public Response handleSimpleObjectWithoutBeanAnnotation(Simple simple) {
        return Response.ok(simple).build();
    }

    @POST
    @Path("/simple-bean")
    public Response handleSimpleBeanObjectWithBeanAnnotation(@BeanParam SimpleBean bean) {
        return Response.ok(bean).build();
    }

    @POST
    @Path("/param-combination")
    public Response handleFormQueryHeaderParamAndReturnConstructedSimpleObject(@FormParam("id") String id,
                                                                               @QueryParam("name") String name,
                                                                               @HeaderParam("address") String address) {
        Simple simple = new Simple(id, name, null, address);
        return Response.ok(simple).build();
    }

    @POST
    @Path("/complex-bean")
    public Response handleComplexBeanAnnotationWithMultipleDifferentParamAnnotations(@BeanParam ComplexBean bean) {
        return Response.ok(bean).build();
    }

}
