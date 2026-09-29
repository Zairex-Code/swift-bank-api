package org.zairex_code.domain.port.in.query;

import org.zairex_code.domain.model.Transaction;

import java.util.List;

public interface TransactionQueryUseCase {

    List<Transaction> getAllTransactions();
}
