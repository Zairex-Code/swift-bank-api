package org.zairex_code.adapter.in.seed;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.zairex_code.domain.model.Account;
import org.zairex_code.domain.port.in.command.AccountCommandUseCase;
import org.zairex_code.domain.port.out.AccountQueryPort;

import java.math.BigDecimal;

@ApplicationScoped
public class SeedDataLoader {

    private static final Logger LOG = Logger.getLogger(SeedDataLoader.class);

    private final AccountQueryPort accountQueryPort;
    private final AccountCommandUseCase accountCommandUseCase;
    private final boolean enabled;

    @Inject
    public SeedDataLoader(AccountQueryPort accountQueryPort,
                          AccountCommandUseCase accountCommandUseCase,
                          @ConfigProperty(name = "app.seed.enabled", defaultValue = "false") boolean enabled) {
        this.accountQueryPort = accountQueryPort;
        this.accountCommandUseCase = accountCommandUseCase;
        this.enabled = enabled;
    }

    void onStart(@Observes StartupEvent event) {
        if (!enabled) {
            LOG.info("Seed disabled (app.seed.enabled=false)");
            return;
        }
        if (!accountQueryPort.findAll().isEmpty()) {
            LOG.info("Seed skipped, accounts already present");
            return;
        }

        accountCommandUseCase.createAccount(Account.builder()
                .accountNumber("ACC-1001")
                .holderName("Alice Johnson")
                .balance(new BigDecimal("1500.00"))
                .build());
        accountCommandUseCase.createAccount(Account.builder()
                .accountNumber("ACC-1002")
                .holderName("Bob Smith")
                .balance(new BigDecimal("800.50"))
                .build());

        LOG.info("Seeded 2 demo accounts");
    }
}
