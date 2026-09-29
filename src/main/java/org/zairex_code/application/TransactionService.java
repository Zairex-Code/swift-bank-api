package org.zairex_code.application;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.zairex_code.domain.model.Transaction;
import org.zairex_code.domain.model.TransactionStatus;
import org.zairex_code.domain.port.in.TransactionUseCase;
import org.zairex_code.domain.port.out.AccountRepositoryPort;
import org.zairex_code.domain.port.out.TransactionRepositoryPort;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@ApplicationScoped
@RequiredArgsConstructor
public class TransactionService implements TransactionUseCase {

    private final AccountRepositoryPort accountRepositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;

    @Override
    public Transaction deposit(String accountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            return recordRejectedTransaction(null, accountId, amount, "Deposit amount must be greater than zero");
        }

        BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_EVEN);

        if (accountRepositoryPort.findById(accountId).isEmpty()){
            return recordRejectedTransaction(null, accountId, normalizedAmount, "Account not found: " + accountId);
        }

        boolean deposited = accountRepositoryPort.deposit(accountId, normalizedAmount);

        if (!deposited){
            return recordRejectedTransaction(null, accountId, normalizedAmount, "Failed to deposit funds into account: " + accountId);
        }

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .sourceAccountId(null)
                .targetAccountId(accountId)
                .amount(normalizedAmount)
                .status(TransactionStatus.COMPLETED)
                .rejectionReason(null)
                .timestamp(LocalDateTime.now())
                .build();
        return transactionRepositoryPort.save(transaction);
    }

    @Override
    public Transaction transfer(String sourceAccountId, String targetAccountId, BigDecimal amount) {
        if (sourceAccountId == null || targetAccountId == null || sourceAccountId.isBlank() || targetAccountId.isBlank()){
            return recordRejectedTransaction(sourceAccountId, targetAccountId, amount, "Source and target account IDs must be privided");
        }

        if (sourceAccountId.equals(targetAccountId)){
            return recordRejectedTransaction(sourceAccountId, targetAccountId, amount, "Source and target accounts must be differet");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            return recordRejectedTransaction(sourceAccountId, targetAccountId, amount, "Transfer amount must be greater than zero");
        }

        BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_EVEN);

        if (accountRepositoryPort.findById(sourceAccountId).isEmpty()){
            return recordRejectedTransaction(sourceAccountId, targetAccountId, normalizedAmount, "Source account not found: " + sourceAccountId);
        }
        
        if (accountRepositoryPort.findById(targetAccountId).isEmpty()){
            return recordRejectedTransaction(sourceAccountId, targetAccountId, normalizedAmount, "Target account not found " + targetAccountId);
        }

        boolean debited = accountRepositoryPort.withdraw(sourceAccountId, normalizedAmount);
        if (!debited){
            return recordRejectedTransaction(sourceAccountId, targetAccountId, normalizedAmount, "Insufficient balance in source account: " + sourceAccountId);
        }

        boolean credited = accountRepositoryPort.deposit(targetAccountId, normalizedAmount);
        if (!credited){
            accountRepositoryPort.deposit(sourceAccountId,normalizedAmount);
            return recordRejectedTransaction(sourceAccountId, targetAccountId, normalizedAmount, "Transfer aborded during crediting. Balance refunded to source account");
        }

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .sourceAccountId(sourceAccountId)
                .targetAccountId(targetAccountId)
                .amount(normalizedAmount)
                .status(TransactionStatus.COMPLETED)
                .rejectionReason(null)
                .timestamp(LocalDateTime.now())
                .build();

        return transactionRepositoryPort.save(transaction);
    }

    @Override
    public List getAllTransaction() {
       return transactionRepositoryPort.findAll();
    }


    private Transaction recordRejectedTransaction(String sourceId, String targetId, BigDecimal amount, String reason){
        BigDecimal safeAmount = (amount != null && amount.compareTo(BigDecimal.ZERO) > 0)
                ? amount.setScale(2, RoundingMode.HALF_EVEN)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .sourceAccountId(sourceId)
                .targetAccountId(targetId)
                .amount(safeAmount)
                .status(TransactionStatus.REJECTED)
                .rejectionReason(reason)
                .timestamp(LocalDateTime.now())
                .build();
        return transactionRepositoryPort.save(transaction);
    }
}
