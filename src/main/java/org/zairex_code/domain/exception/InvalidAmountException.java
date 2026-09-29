package org.zairex_code.domain.exception;

public class InvalidAmountException extends BankingException {

    public InvalidAmountException(String message) {
        super(message);
    }
}
