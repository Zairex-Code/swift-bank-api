package org.zairex_code.domain.exception;

public class TransferFailedException extends BankingException {

    public TransferFailedException(String message) {
        super(message);
    }

    public TransferFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
