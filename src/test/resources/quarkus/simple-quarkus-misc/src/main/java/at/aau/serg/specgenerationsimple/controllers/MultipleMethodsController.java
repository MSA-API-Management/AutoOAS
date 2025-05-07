package at.aau.serg.specgenerationsimple.controllers;

import at.aau.serg.specgenerationsimple.models.SimpleObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("")
public class MultipleMethodsController {
    /*   //** TODO does not work in quarkus
     * This allows all http methods
     *
     * @param num
     * @return
     *//*
    @RequestMapping("/unspecified-method")
    public SimpleObject unspecifiedMethod(@RequestHeader(value = "num") int num) {
        return new SimpleObject("Echo", num);
    }
    */

    @GET
    @Path("/get-and-post-method")
    public SimpleObject getMethod(@HeaderParam("num") int num) {
        return new SimpleObject("Echo", num);
    }

    @POST
    @Path("/get-and-post-method")
    public SimpleObject postMethod(@HeaderParam("num") int num) {
        return new SimpleObject("Echo", num);
    }

    @POST
    @Path("/get-and-post-method-reqmapping")
    public SimpleObject postRequestMethod(@HeaderParam("num") int num) {
        return new SimpleObject("Echo", num);
    }

    @GET
    @Path("/get-and-post-method-reqmapping")
    public SimpleObject getRequestMethod(@HeaderParam("num") int num) {
        return new SimpleObject("Echo", num);
    }
}
