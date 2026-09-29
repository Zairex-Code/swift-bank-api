package org.zairex_code.domain.port.in.command;

import org.zairex_code.domain.model.Transaction;

import java.math.BigDecimal;

public interface TransactionCommandUseCase {

    Transaction deposit(String accountId, BigDecimal amount);

    Transaction transfer(String sourceAccountId, String targetAccountId, BigDecimal amount);
}
