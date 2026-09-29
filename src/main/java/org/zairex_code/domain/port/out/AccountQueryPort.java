package org.zairex_code.domain.port.out;

import org.zairex_code.domain.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountQueryPort {

    Optional<Account> findById(String id);

    List<Account> findAll();
}
