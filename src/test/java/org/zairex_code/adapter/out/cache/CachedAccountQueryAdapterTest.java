package org.zairex_code.adapter.out.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zairex_code.domain.model.Account;
import org.zairex_code.domain.port.out.AccountCachePort;
import org.zairex_code.domain.port.out.AccountQueryPort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CachedAccountQueryAdapterTest {

    @Mock
    AccountQueryPort delegate;

    @Mock
    AccountCachePort cachePort;

    @InjectMocks
    CachedAccountQueryAdapter adapter;

    @Test
    void findByIdReturnsCachedAccountWithoutHittingDatabase() {
        Account account = Account.builder().id("a1").build();
        when(cachePort.get("a1")).thenReturn(Optional.of(account));

        assertEquals(Optional.of(account), adapter.findById("a1"));
        verify(delegate, never()).findById(any());
    }

    @Test
    void findByIdOnCacheMissLoadsFromDatabaseAndPopulatesCache() {
        Account account = Account.builder().id("a1").build();
        when(cachePort.get("a1")).thenReturn(Optional.empty());
        when(delegate.findById("a1")).thenReturn(Optional.of(account));

        assertEquals(Optional.of(account), adapter.findById("a1"));
        verify(cachePort).put(account);
    }

    @Test
    void findByIdOnCacheMissAndMissingAccountDoesNotPopulateCache() {
        when(cachePort.get("a1")).thenReturn(Optional.empty());
        when(delegate.findById("a1")).thenReturn(Optional.empty());

        assertEquals(Optional.empty(), adapter.findById("a1"));
        verify(cachePort, never()).put(any());
    }

    @Test
    void findAllDelegatesDirectlyToDatabase() {
        List<Account> accounts = List.of(Account.builder().id("a1").build());
        when(delegate.findAll()).thenReturn(accounts);

        assertEquals(accounts, adapter.findAll());
        verify(delegate).findAll();
    }
}
