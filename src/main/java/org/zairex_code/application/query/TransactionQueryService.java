package org.zairex_code.application.query;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.zairex_code.domain.model.Transaction;
import org.zairex_code.domain.port.in.query.TransactionQueryUseCase;
import org.zairex_code.domain.port.out.TransactionQueryPort;

import java.util.List;

@ApplicationScoped
public class TransactionQueryService implements TransactionQueryUseCase {

    private final TransactionQueryPort transactionQueryPort;

    @Inject
    public TransactionQueryService(TransactionQueryPort transactionQueryPort) {
        this.transactionQueryPort = transactionQueryPort;
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionQueryPort.findAll();
    }
}
