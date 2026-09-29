package org.zairex_code.application.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zairex_code.domain.exception.BankingException;
import org.zairex_code.domain.exception.DuplicateAccountException;
import org.zairex_code.domain.model.Account;
import org.zairex_code.domain.port.out.AccountCachePort;
import org.zairex_code.domain.port.out.AccountCommandPort;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountCommandServiceTest {

    @Mock
    AccountCommandPort accountCommandPort;

    @Mock
    AccountCachePort accountCachePort;

    @InjectMocks
    AccountCommandService service;

    @Test
    void createAccountAssignsIdAndDefaultBalanceAndEvictsCache() {
        Account input = Account.builder().accountNumber("ACC-1").holderName("Alice").build();
        when(accountCommandPort.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account result = service.createAccount(input);

        assertNotNull(result.getId());
        assertEquals(new BigDecimal("0.00"), result.getBalance());
        verify(accountCachePort).evict(result.getId());
    }

    @Test
    void createAccountKeepsProvidedBalanceScaled() {
        Account input = Account.builder()
                .accountNumber("ACC-1")
                .holderName("Alice")
                .balance(new BigDecimal("10"))
                .build();
        when(accountCommandPort.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account result = service.createAccount(input);

        assertEquals(new BigDecimal("10.00"), result.getBalance());
    }

    @Test
    void createAccountThrowsWhenDuplicate() {
        when(accountCommandPort.existsByAccountNumber("ACC-1")).thenReturn(true);
        Account input = Account.builder().accountNumber("ACC-1").holderName("Alice").build();

        assertThrows(DuplicateAccountException.class, () -> service.createAccount(input));
        verify(accountCommandPort, never()).save(any());
    }

    @Test
    void createAccountThrowsWhenNull() {
        assertThrows(BankingException.class, () -> service.createAccount(null));
    }
}
