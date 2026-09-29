package org.zairex_code.adapter.out.cache;

import jakarta.annotation.Priority;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import org.zairex_code.domain.model.Account;
import org.zairex_code.domain.port.out.AccountCachePort;
import org.zairex_code.domain.port.out.AccountQueryPort;

import java.util.List;
import java.util.Optional;

@Decorator
@Priority(1)
public class CachedAccountQueryAdapter implements AccountQueryPort {

    @Inject
    @Any
    @Delegate
    AccountQueryPort delegate;

    @Inject
    AccountCachePort cachePort;

    @Override
    public Optional<Account> findById(String id) {
        Optional<Account> cached = cachePort.get(id);
        if (cached.isPresent()) {
            return cached;
        }
        Optional<Account> fromDatabase = delegate.findById(id);
        fromDatabase.ifPresent(cachePort::put);
        return fromDatabase;
    }

    @Override
    public List<Account> findAll() {
        return delegate.findAll();
    }
}
