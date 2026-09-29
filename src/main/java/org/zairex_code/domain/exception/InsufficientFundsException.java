package org.zairex_code.domain.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends BankingException {

    public InsufficientFundsException(String accountId, BigDecimal balance, BigDecimal requested) {
        super("Insufficient balance in account " + accountId
                + " (available=" + balance + ", requested=" + requested + ")");
    }
}
