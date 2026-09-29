package org.zairex_code.application.command;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.zairex_code.domain.exception.AccountNotFoundException;
import org.zairex_code.domain.exception.InsufficientFundsException;
import org.zairex_code.domain.exception.InvalidAmountException;
import org.zairex_code.domain.exception.TransferFailedException;
import org.zairex_code.domain.model.Account;
import org.zairex_code.domain.model.Transaction;
import org.zairex_code.domain.model.TransactionStatus;
import org.zairex_code.domain.port.in.command.TransactionCommandUseCase;
import org.zairex_code.domain.port.out.AccountCachePort;
import org.zairex_code.domain.port.out.AccountCommandPort;
import org.zairex_code.domain.port.out.AccountQueryPort;
import org.zairex_code.domain.port.out.TransactionCommandPort;
import org.zairex_code.domain.port.out.TransactionManagerPort;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class TransactionCommandService implements TransactionCommandUseCase {

    private final AccountQueryPort accountQueryPort;
    private final AccountCommandPort accountCommandPort;
    private final TransactionCommandPort transactionCommandPort;
    private final TransactionManagerPort transactionManagerPort;
    private final AccountCachePort accountCachePort;

    @Inject
    public TransactionCommandService(AccountQueryPort accountQueryPort,
                                     AccountCommandPort accountCommandPort,
                                     TransactionCommandPort transactionCommandPort,
                                     TransactionManagerPort transactionManagerPort,
                                     AccountCachePort accountCachePort) {
        this.accountQueryPort = accountQueryPort;
        this.accountCommandPort = accountCommandPort;
        this.transactionCommandPort = transactionCommandPort;
        this.transactionManagerPort = transactionManagerPort;
        this.accountCachePort = accountCachePort;
    }

    @Override
    public Transaction deposit(String accountId, BigDecimal amount) {
        if (accountId == null || accountId.isBlank()) {
            persistRejected(null, accountId, amount, "Account id is required");
            throw new AccountNotFoundException(String.valueOf(accountId));
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            persistRejected(null, accountId, amount, "Deposit amount must be greater than zero");
            throw new InvalidAmountException("Deposit amount must be greater than zero");
        }

        BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_EVEN);

        if (accountQueryPort.findById(accountId).isEmpty()) {
            persistRejected(null, accountId, normalized, "Account not found: " + accountId);
            throw new AccountNotFoundException(accountId);
        }

        Transaction completed = transactionManagerPort.execute(() -> {
            if (!accountCommandPort.credit(accountId, normalized)) {
                throw new TransferFailedException("Failed to deposit into account: " + accountId);
            }
            return transactionCommandPort.save(completedTransaction(null, accountId, normalized));
        });

        accountCachePort.evict(accountId);
        return completed;
    }

    @Override
    public Transaction transfer(String sourceAccountId, String targetAccountId, BigDecimal amount) {
        if (isBlank(sourceAccountId) || isBlank(targetAccountId)) {
            persistRejected(sourceAccountId, targetAccountId, amount,
                    "Source and target account IDs must be provided");
            throw new InvalidAmountException("Source and target account IDs must be provided");
        }
        if (sourceAccountId.equals(targetAccountId)) {
            persistRejected(sourceAccountId, targetAccountId, amount,
                    "Source and target accounts must differ");
            throw new InvalidAmountException("Source and target accounts must differ");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            persistRejected(sourceAccountId, targetAccountId, amount,
                    "Transfer amount must be greater than zero");
            throw new InvalidAmountException("Transfer amount must be greater than zero");
        }

        BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_EVEN);

        Account source = accountQueryPort.findById(sourceAccountId).orElseGet(() -> {
            persistRejected(sourceAccountId, targetAccountId, normalized,
                    "Source account not found: " + sourceAccountId);
            throw new AccountNotFoundException(sourceAccountId);
        });

        accountQueryPort.findById(targetAccountId).orElseGet(() -> {
            persistRejected(sourceAccountId, targetAccountId, normalized,
                    "Target account not found: " + targetAccountId);
            throw new AccountNotFoundException(targetAccountId);
        });

        if (source.getBalance().compareTo(normalized) < 0) {
            persistRejected(sourceAccountId, targetAccountId, normalized,
                    "Insufficient balance in source account: " + sourceAccountId);
            throw new InsufficientFundsException(sourceAccountId, source.getBalance(), normalized);
        }

        Transaction completed = transactionManagerPort.execute(() -> {
            if (!accountCommandPort.debit(sourceAccountId, normalized)) {
                throw new TransferFailedException("Insufficient balance in source account: " + sourceAccountId);
            }
            if (!accountCommandPort.credit(targetAccountId, normalized)) {
                throw new TransferFailedException("Target account could not be credited: " + targetAccountId);
            }
            return transactionCommandPort.save(completedTransaction(sourceAccountId, targetAccountId, normalized));
        });

        accountCachePort.evict(sourceAccountId);
        accountCachePort.evict(targetAccountId);
        return completed;
    }

    private Transaction completedTransaction(String sourceAccountId, String targetAccountId, BigDecimal amount) {
        return Transaction.builder()
                .id(UUID.randomUUID().toString())
                .sourceAccountId(sourceAccountId)
                .targetAccountId(targetAccountId)
                .amount(amount)
                .status(TransactionStatus.COMPLETED)
                .rejectionReason(null)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private void persistRejected(String sourceAccountId, String targetAccountId, BigDecimal amount, String reason) {
        BigDecimal safeAmount = (amount != null && amount.compareTo(BigDecimal.ZERO) > 0)
                ? amount.setScale(2, RoundingMode.HALF_EVEN)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);

        Transaction rejected = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .sourceAccountId(sourceAccountId)
                .targetAccountId(targetAccountId)
                .amount(safeAmount)
                .status(TransactionStatus.REJECTED)
                .rejectionReason(reason)
                .timestamp(LocalDateTime.now())
                .build();
        transactionCommandPort.save(rejected);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
