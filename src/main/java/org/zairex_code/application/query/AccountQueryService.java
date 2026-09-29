package org.zairex_code.application.query;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.zairex_code.domain.exception.AccountNotFoundException;
import org.zairex_code.domain.model.Account;
import org.zairex_code.domain.port.in.query.AccountQueryUseCase;
import org.zairex_code.domain.port.out.AccountQueryPort;

import java.util.List;

@ApplicationScoped
public class AccountQueryService implements AccountQueryUseCase {

    private final AccountQueryPort accountQueryPort;

    @Inject
    public AccountQueryService(AccountQueryPort accountQueryPort) {
        this.accountQueryPort = accountQueryPort;
    }

    @Override
    public Account getAccountById(String id) {
        if (id == null || id.isBlank()) {
            throw new AccountNotFoundException(String.valueOf(id));
        }
        return accountQueryPort.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountQueryPort.findAll();
    }
}
