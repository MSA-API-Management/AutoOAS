package at.aau.serg.resources;

import at.aau.serg.models.GetterConstraints;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/getter-constraints")
public class GetterConstraintsResource {
    @Path("/constraints")
    @POST
    public GetterConstraints handleConstraintsInReturnValue(String input) {
        return new GetterConstraints();
    }

    @Path("/property-constraints-getter")
    @POST
    public GetterConstraints handleConstraintsInReturnValueWithGetter(String input) {
        return new GetterConstraints();
    }
}
