package org.zairex_code.domain.port.out;

import org.zairex_code.domain.model.Transaction;

import java.util.List;

public interface TransactionQueryPort {

    List<Transaction> findAll();
}
