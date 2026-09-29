package org.zairex_code.domain.port.out;

import org.zairex_code.domain.model.Account;

import java.util.Optional;

public interface AccountCachePort {

    Optional<Account> get(String accountId);

    void put(Account account);

    void evict(String accountId);
}
