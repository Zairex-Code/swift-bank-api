package org.zairex_code.domain.port.out;

import org.zairex_code.domain.model.Transaction;

public interface TransactionCommandPort {

    Transaction save(Transaction transaction);
}
