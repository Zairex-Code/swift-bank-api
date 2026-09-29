package org.zairex_code.application.command;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.zairex_code.domain.exception.BankingException;
import org.zairex_code.domain.exception.DuplicateAccountException;
import org.zairex_code.domain.model.Account;
import org.zairex_code.domain.port.in.command.AccountCommandUseCase;
import org.zairex_code.domain.port.out.AccountCachePort;
import org.zairex_code.domain.port.out.AccountCommandPort;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@ApplicationScoped
public class AccountCommandService implements AccountCommandUseCase {

    private final AccountCommandPort accountCommandPort;
    private final AccountCachePort accountCachePort;

    @Inject
    public AccountCommandService(AccountCommandPort accountCommandPort, AccountCachePort accountCachePort) {
        this.accountCommandPort = accountCommandPort;
        this.accountCachePort = accountCachePort;
    }

    @Override
    public Account createAccount(Account account) {
        if (account == null) {
            throw new BankingException("Account payload is required");
        }
        if (account.getAccountNumber() != null
                && accountCommandPort.existsByAccountNumber(account.getAccountNumber())) {
            throw new DuplicateAccountException(account.getAccountNumber());
        }
        if (account.getId() == null || account.getId().isBlank()) {
            account.setId(UUID.randomUUID().toString());
        }

        BigDecimal balance = account.getBalance() != null
                ? account.getBalance().setScale(2, RoundingMode.HALF_EVEN)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        account.setBalance(balance);

        Account saved = accountCommandPort.save(account);
        accountCachePort.evict(saved.getId());
        return saved;
    }
}
