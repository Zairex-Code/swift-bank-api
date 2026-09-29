package org.zairex_code.adapter.in.rest.error;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GenericExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        LOG.error("Unhandled exception", exception);
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        ErrorResponse body = new ErrorResponse(
                status.getStatusCode(),
                status.getReasonPhrase(),
                "Unexpected error processing the request",
                LocalDateTime.now());
        return Response.status(status).entity(body).type(MediaType.APPLICATION_JSON).build();
    }
}
