package org.zairex_code.domain.exception;

public class DuplicateAccountException extends BankingException {

    public DuplicateAccountException(String accountNumber) {
        super("An account with number " + accountNumber + " already exists");
    }
}
