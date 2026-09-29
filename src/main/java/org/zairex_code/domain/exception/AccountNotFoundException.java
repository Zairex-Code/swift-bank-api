package org.zairex_code.domain.exception;

public class AccountNotFoundException extends BankingException {

    public AccountNotFoundException(String accountId) {
        super("Account not found: " + accountId);
    }
}
