package org.zairex_code.domain.port.out;

import org.zairex_code.domain.model.Account;

import java.math.BigDecimal;

public interface AccountCommandPort {

    Account save(Account account);

    boolean existsByAccountNumber(String accountNumber);

    boolean debit(String accountId, BigDecimal amount);

    boolean credit(String accountId, BigDecimal amount);
}
