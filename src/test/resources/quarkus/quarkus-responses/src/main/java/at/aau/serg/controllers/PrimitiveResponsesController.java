package at.aau.serg.controllers;

import at.aau.serg.models.Simple;
import at.aau.serg.models.SimpleEnum;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Path("/primitive-responses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PrimitiveResponsesController {

    // fixme unspecified type
    @GET
    @Path("/int")
    public int getInt() {
        return 5;
    }

    @GET
    @Path("/str")
    public String getString() {
        return "hello";
    }

    // fixme unspecified type
    @GET
    @Path("/double")
    public double getDouble() {
        return 5.1;
    }

    // fixme unspecified type
    @GET
    @Path("/bool")
    public boolean getBool() {
        return true;
    }

    @GET
    @Path("/simple-enum")
    public SimpleEnum getEnum() {
        return SimpleEnum.BAD;
    }

    @GET
    @Path("/simple-obj")
    public Simple getObj() {
        return new Simple().withName("Peter").withId(1);
    }

    @GET
    @Path("/string-echo")
    @Produces(MediaType.TEXT_PLAIN)
    public String getPlainText(@QueryParam("id") String id) {
        return "ID: " + id;
    }

    @DELETE
    @Path("map/{id}")
    public Map<String, Boolean> deleteMapResp(@PathParam("id") Long id) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return response;
    }

    @GET
    @Path("/void-response")
    public void getAllNoReturn() {
        ArrayList<Simple> al = new ArrayList<>();
        al.add(new Simple().withName("Peter").withId(1));
    }

    @DELETE
    @Path("list/{id}")
    public List<String> deleteListResp(@PathParam("id") Long id) {
        return List.of("deleted");
    }
}
