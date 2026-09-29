package org.zairex_code.adapter.out.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Updates;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.zairex_code.domain.model.Account;
import org.zairex_code.domain.port.out.AccountCommandPort;

import java.math.BigDecimal;
import java.math.RoundingMode;

@ApplicationScoped
public class MongoAccountCommandAdapter implements AccountCommandPort {

    private static final String ACCOUNTS_COLLECTION = "accounts";

    private final MongoCollection<Document> accounts;
    private final MongoTransactionContext transactionContext;

    @Inject
    public MongoAccountCommandAdapter(MongoClient mongoClient,
                                      MongoTransactionContext transactionContext,
                                      @ConfigProperty(name = "quarkus.mongodb.database") String databaseName) {
        this.accounts = mongoClient.getDatabase(databaseName).getCollection(ACCOUNTS_COLLECTION);
        this.transactionContext = transactionContext;
    }

    @Override
    public Account save(Account account) {
        Document document = MongoDocumentMapper.toDocument(account);
        accounts.replaceOne(transactionContext.current(),
                Filters.eq("_id", account.getId()),
                document,
                new ReplaceOptions().upsert(true));
        return account;
    }

    @Override
    public boolean existsByAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            return false;
        }
        return accounts.countDocuments(Filters.eq("accountNumber", accountNumber)) > 0;
    }

    @Override
    public boolean debit(String accountId, BigDecimal amount) {
        BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_EVEN);
        Bson filter = Filters.and(
                Filters.eq("_id", accountId),
                Filters.gte("balance", new Decimal128(normalized)));
        Bson update = Updates.inc("balance", new Decimal128(normalized.negate()));
        Document updated = accounts.findOneAndUpdate(
                transactionContext.current(),
                filter,
                update,
                new FindOneAndUpdateOptions());
        return updated != null;
    }

    @Override
    public boolean credit(String accountId, BigDecimal amount) {
        BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_EVEN);
        Bson update = Updates.inc("balance", new Decimal128(normalized));
        return accounts.updateOne(transactionContext.current(), Filters.eq("_id", accountId), update)
                .getMatchedCount() > 0;
    }
}
