package org.zairex_code.domain.port.in.command;

import org.zairex_code.domain.model.Account;

public interface AccountCommandUseCase {

    Account createAccount(Account account);
}
