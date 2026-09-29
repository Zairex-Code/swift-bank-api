package org.zairex_code.application.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zairex_code.domain.exception.AccountNotFoundException;
import org.zairex_code.domain.exception.InsufficientFundsException;
import org.zairex_code.domain.exception.InvalidAmountException;
import org.zairex_code.domain.model.Account;
import org.zairex_code.domain.model.Transaction;
import org.zairex_code.domain.model.TransactionStatus;
import org.zairex_code.domain.port.out.AccountCachePort;
import org.zairex_code.domain.port.out.AccountCommandPort;
import org.zairex_code.domain.port.out.AccountQueryPort;
import org.zairex_code.domain.port.out.TransactionCommandPort;
import org.zairex_code.domain.port.out.TransactionManagerPort;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionCommandServiceTest {

    @Mock
    AccountQueryPort accountQueryPort;

    @Mock
    AccountCommandPort accountCommandPort;

    @Mock
    TransactionCommandPort transactionCommandPort;

    @Mock
    TransactionManagerPort transactionManagerPort;

    @Mock
    AccountCachePort accountCachePort;

    @InjectMocks
    TransactionCommandService service;

    private void runTransactionSynchronously() {
        when(transactionManagerPort.execute(any())).thenAnswer(invocation -> {
            Supplier<?> operation = invocation.getArgument(0);
            return operation.get();
        });
    }

    @Test
    void depositCompletesAndEvictsCache() {
        when(accountQueryPort.findById("a1"))
                .thenReturn(Optional.of(Account.builder().id("a1").balance(new BigDecimal("100.00")).build()));
        when(accountCommandPort.credit(eq("a1"), eq(new BigDecimal("50.00")))).thenReturn(true);
        when(transactionCommandPort.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        runTransactionSynchronously();

        Transaction transaction = service.deposit("a1", new BigDecimal("50.00"));

        assertEquals(TransactionStatus.COMPLETED, transaction.getStatus());
        assertEquals(new BigDecimal("50.00"), transaction.getAmount());
        verify(accountCachePort).evict("a1");
    }

    @Test
    void depositRejectsInvalidAmountAndPersistsAudit() {
        when(transactionCommandPort.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        assertThrows(InvalidAmountException.class, () -> service.deposit("a1", new BigDecimal("-5")));
        verify(transactionCommandPort).save(argThat(t -> t.getStatus() == TransactionStatus.REJECTED));
    }

    @Test
    void depositThrowsWhenAccountNotFound() {
        when(accountQueryPort.findById("missing")).thenReturn(Optional.empty());
        when(transactionCommandPort.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        assertThrows(AccountNotFoundException.class, () -> service.deposit("missing", new BigDecimal("10.00")));
    }

    @Test
    void transferThrowsOnInsufficientFundsWithoutMutating() {
        when(accountQueryPort.findById("a1"))
                .thenReturn(Optional.of(Account.builder().id("a1").balance(new BigDecimal("10.00")).build()));
        when(accountQueryPort.findById("a2"))
                .thenReturn(Optional.of(Account.builder().id("a2").balance(new BigDecimal("0.00")).build()));
        when(transactionCommandPort.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        assertThrows(InsufficientFundsException.class,
                () -> service.transfer("a1", "a2", new BigDecimal("50.00")));
        verify(accountCommandPort, never()).debit(any(), any());
    }

    @Test
    void transferCompletesAtomicallyAndEvictsBothAccounts() {
        when(accountQueryPort.findById("a1"))
                .thenReturn(Optional.of(Account.builder().id("a1").balance(new BigDecimal("100.00")).build()));
        when(accountQueryPort.findById("a2"))
                .thenReturn(Optional.of(Account.builder().id("a2").balance(new BigDecimal("0.00")).build()));
        when(accountCommandPort.debit(eq("a1"), eq(new BigDecimal("40.00")))).thenReturn(true);
        when(accountCommandPort.credit(eq("a2"), eq(new BigDecimal("40.00")))).thenReturn(true);
        when(transactionCommandPort.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        runTransactionSynchronously();

        Transaction transaction = service.transfer("a1", "a2", new BigDecimal("40.00"));

        assertEquals(TransactionStatus.COMPLETED, transaction.getStatus());
        verify(accountCachePort).evict("a1");
        verify(accountCachePort).evict("a2");
    }

    @Test
    void transferThrowsWhenSourceAndTargetAreTheSame() {
        when(transactionCommandPort.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        assertThrows(InvalidAmountException.class,
                () -> service.transfer("a1", "a1", new BigDecimal("10.00")));
    }
}
