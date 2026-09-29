package org.zairex_code.adapter.in.rest.error;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.zairex_code.domain.exception.AccountNotFoundException;
import org.zairex_code.domain.exception.BankingException;
import org.zairex_code.domain.exception.DuplicateAccountException;
import org.zairex_code.domain.exception.InsufficientFundsException;
import org.zairex_code.domain.exception.InvalidAmountException;
import org.zairex_code.domain.exception.TransferFailedException;

import java.time.LocalDateTime;

@Provider
public class BankingExceptionMapper implements ExceptionMapper<BankingException> {

    @Override
    public Response toResponse(BankingException exception) {
        Response.Status status = resolveStatus(exception);
        ErrorResponse body = new ErrorResponse(
                status.getStatusCode(),
                status.getReasonPhrase(),
                exception.getMessage(),
                LocalDateTime.now());
        return Response.status(status).entity(body).type(MediaType.APPLICATION_JSON).build();
    }

    private Response.Status resolveStatus(BankingException exception) {
        if (exception instanceof AccountNotFoundException) {
            return Response.Status.NOT_FOUND;
        }
        if (exception instanceof DuplicateAccountException) {
            return Response.Status.CONFLICT;
        }
        if (exception instanceof InvalidAmountException) {
            return Response.Status.BAD_REQUEST;
        }
        if (exception instanceof InsufficientFundsException || exception instanceof TransferFailedException) {
            return Response.Status.UNPROCESSABLE_ENTITY;
        }
        return Response.Status.BAD_REQUEST;
    }
}
