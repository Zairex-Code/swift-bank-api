package org.zairex_code.application.query;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zairex_code.domain.exception.AccountNotFoundException;
import org.zairex_code.domain.model.Account;
import org.zairex_code.domain.port.out.AccountQueryPort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountQueryServiceTest {

    @Mock
    AccountQueryPort accountQueryPort;

    @InjectMocks
    AccountQueryService service;

    @Test
    void getAccountByIdReturnsAccountWhenPresent() {
        Account account = Account.builder().id("a1").accountNumber("ACC-1").build();
        when(accountQueryPort.findById("a1")).thenReturn(Optional.of(account));

        assertEquals(account, service.getAccountById("a1"));
    }

    @Test
    void getAccountByIdThrowsWhenMissing() {
        when(accountQueryPort.findById("missing")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> service.getAccountById("missing"));
    }

    @Test
    void getAccountByIdThrowsWhenBlank() {
        assertThrows(AccountNotFoundException.class, () -> service.getAccountById(" "));
    }

    @Test
    void getAllAccountsDelegatesToPort() {
        List<Account> accounts = List.of(Account.builder().id("a1").build());
        when(accountQueryPort.findAll()).thenReturn(accounts);

        assertEquals(accounts, service.getAllAccounts());
        verify(accountQueryPort).findAll();
    }
}
