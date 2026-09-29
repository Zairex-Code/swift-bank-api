package org.zairex_code.adapter.out.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.mutiny.redis.client.Redis;
import io.vertx.mutiny.redis.client.RedisAPI;
import io.vertx.mutiny.redis.client.Response;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.zairex_code.domain.model.Account;
import org.zairex_code.domain.port.out.AccountCachePort;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RedisAccountCacheAdapter implements AccountCachePort {

    private static final Logger LOG = Logger.getLogger(RedisAccountCacheAdapter.class);
    private static final String KEY_PREFIX = "account:";

    private final RedisAPI redis;
    private final ObjectMapper objectMapper;
    private final long ttlSeconds;

    @Inject
    public RedisAccountCacheAdapter(Redis redisClient,
                                    ObjectMapper objectMapper,
                                    @ConfigProperty(name = "app.cache.account-ttl-seconds", defaultValue = "60")
                                    long ttlSeconds) {
        this.redis = RedisAPI.api(redisClient);
        this.objectMapper = objectMapper;
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    public Optional<Account> get(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            return Optional.empty();
        }
        try {
            Response response = redis.get(key(accountId)).await().indefinitely();
            if (response == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(response.toString(), Account.class));
        } catch (RuntimeException | JsonProcessingException exception) {
            LOG.warnv("Redis GET failed for {0}: {1}", accountId, exception.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void put(Account account) {
        if (account == null || account.getId() == null) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(account);
            List<String> command = Arrays.asList(
                    key(account.getId()), json, "EX", Long.toString(ttlSeconds));
            redis.set(command).await().indefinitely();
        } catch (RuntimeException | JsonProcessingException exception) {
            LOG.warnv("Redis SET failed for {0}: {1}", account.getId(), exception.getMessage());
        }
    }

    @Override
    public void evict(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            return;
        }
        try {
            redis.del(Collections.singletonList(key(accountId))).await().indefinitely();
        } catch (RuntimeException exception) {
            LOG.warnv("Redis DEL failed for {0}: {1}", accountId, exception.getMessage());
        }
    }

    private String key(String accountId) {
        return KEY_PREFIX + accountId;
    }
}
