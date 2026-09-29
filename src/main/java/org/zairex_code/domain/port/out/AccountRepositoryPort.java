package org.zairex_code.domain.port.out;

import org.zairex_code.domain.model.Account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepositoryPort {

    Account save(Account account);

    Optional findById(String id);

    List findAll();

    boolean withdraw(String accountId, BigDecimal amount);

    boolean deposit(String accountId, BigDecimal amount);
}
