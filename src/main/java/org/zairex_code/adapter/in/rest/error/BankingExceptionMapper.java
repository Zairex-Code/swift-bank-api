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

    private static final int BAD_REQUEST = 400;
    private static final int NOT_FOUND = 404;
    private static final int CONFLICT = 409;
    private static final int UNPROCESSABLE_ENTITY = 422;

    @Override
    public Response toResponse(BankingException exception) {
        int status = resolveStatus(exception);
        ErrorResponse body = new ErrorResponse(
                status,
                reasonPhrase(status),
                exception.getMessage(),
                LocalDateTime.now());
        return Response.status(status).entity(body).type(MediaType.APPLICATION_JSON).build();
    }

    private int resolveStatus(BankingException exception) {
        if (exception instanceof AccountNotFoundException) {
            return NOT_FOUND;
        }
        if (exception instanceof DuplicateAccountException) {
            return CONFLICT;
        }
        if (exception instanceof InvalidAmountException) {
            return BAD_REQUEST;
        }
        if (exception instanceof InsufficientFundsException || exception instanceof TransferFailedException) {
            return UNPROCESSABLE_ENTITY;
        }
        return BAD_REQUEST;
    }

    private String reasonPhrase(int status) {
        return switch (status) {
            case NOT_FOUND -> "Not Found";
            case CONFLICT -> "Conflict";
            case UNPROCESSABLE_ENTITY -> "Unprocessable Entity";
            default -> "Bad Request";
        };
    }
}
