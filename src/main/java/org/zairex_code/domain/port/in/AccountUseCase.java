package org.zairex_code.domain.port.in;

import org.zairex_code.domain.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountUseCase {
    Account createAccount(Account account);

    Optional getAccountById(String id);

    List getAllAccount();
}
