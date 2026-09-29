package org.zairex_code.application;


import jakarta.enterprise.context.ApplicationScoped;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.zairex_code.domain.model.Account;
import org.zairex_code.domain.port.in.AccountUseCase;
import org.zairex_code.domain.port.out.AccountRepositoryPort;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@RequiredArgsConstructor
@ApplicationScoped
public class AccountService implements AccountUseCase {
    private final AccountRepositoryPort accountRepositoryPort;

    @Override
    public Account createAccount(Account account) {
        if (account.getId() == null || account.getId().isBlank()){
            account.setId(UUID.randomUUID().toString());
        }

        BigDecimal initialBalance = account.getBalance() != null
                ? account.getBalance()
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        account.setBalance(initialBalance);

        return accountRepositoryPort.save(account);
    }

    @Override
    public Optional getAccountById(String id) {
        if (id == null || id.isBlank()){
            return Optional.empty();
        }
        return accountRepositoryPort.findById(id);
    }

    @Override
    public List getAllAccount() {
        return accountRepositoryPort.findAll();
    }
}
