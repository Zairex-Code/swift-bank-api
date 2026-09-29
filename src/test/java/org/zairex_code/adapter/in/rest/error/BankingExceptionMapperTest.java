package org.zairex_code.adapter.in.rest.error;

import org.junit.jupiter.api.Test;
import org.zairex_code.domain.exception.AccountNotFoundException;
import org.zairex_code.domain.exception.DuplicateAccountException;
import org.zairex_code.domain.exception.InsufficientFundsException;
import org.zairex_code.domain.exception.InvalidAmountException;
import org.zairex_code.domain.exception.TransferFailedException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BankingExceptionMapperTest {

    private final BankingExceptionMapper mapper = new BankingExceptionMapper();

    @Test
    void accountNotFoundMapsTo404() {
        assertEquals(404, mapper.toResponse(new AccountNotFoundException("a1")).getStatus());
    }

    @Test
    void duplicateAccountMapsTo409() {
        assertEquals(409, mapper.toResponse(new DuplicateAccountException("ACC-1")).getStatus());
    }

    @Test
    void invalidAmountMapsTo400() {
        assertEquals(400, mapper.toResponse(new InvalidAmountException("bad")).getStatus());
    }

    @Test
    void insufficientFundsMapsTo422() {
        assertEquals(422, mapper.toResponse(
                new InsufficientFundsException("a1", new BigDecimal("1.00"), new BigDecimal("2.00"))).getStatus());
    }

    @Test
    void transferFailedMapsTo422() {
        assertEquals(422, mapper.toResponse(new TransferFailedException("failed")).getStatus());
    }
}
