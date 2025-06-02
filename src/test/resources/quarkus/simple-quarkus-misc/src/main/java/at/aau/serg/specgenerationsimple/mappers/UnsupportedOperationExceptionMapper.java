package at.aau.serg.specgenerationsimple.mappers;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class UnsupportedOperationExceptionMapper implements ExceptionMapper<UnsupportedOperationException> {
    @Override
    public Response toResponse(UnsupportedOperationException e) {
        return Response
                .status(Response.Status.NOT_IMPLEMENTED)
                .entity(e.getMessage())
                .build();
    }
}
