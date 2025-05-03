package at.aau.serg;

import at.aau.serg.exceptions.CustomRuntimeException;
import at.aau.serg.models.ErrorResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CustomRuntimeExceptionMapper implements ExceptionMapper<CustomRuntimeException> {
    @Override
    public Response toResponse(CustomRuntimeException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Not Found", exception.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
