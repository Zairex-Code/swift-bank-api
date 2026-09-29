package org.zairex_code.adapter.in.rest.error;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));
        Response.Status status = Response.Status.BAD_REQUEST;
        ErrorResponse body = new ErrorResponse(
                status.getStatusCode(),
                status.getReasonPhrase(),
                message,
                LocalDateTime.now());
        return Response.status(status).entity(body).type(MediaType.APPLICATION_JSON).build();
    }
}
