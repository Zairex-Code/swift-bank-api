package org.zairex_code.domain.port.in.query;

import org.zairex_code.domain.model.Account;

import java.util.List;

public interface AccountQueryUseCase {

    Account getAccountById(String id);

    List<Account> getAllAccounts();
}
