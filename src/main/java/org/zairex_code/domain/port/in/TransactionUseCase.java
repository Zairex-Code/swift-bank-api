package org.zairex_code.domain.port.in;

import org.zairex_code.domain.model.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionUseCase {
    Transaction deposit(String accountId, BigDecimal amount);

    Transaction transfer(String sourceAccountId, String tragetAccountId, BigDecimal amount);

    List getAllTransaction();
}
