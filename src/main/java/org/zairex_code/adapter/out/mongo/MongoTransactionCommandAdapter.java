package org.zairex_code.adapter.out.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.zairex_code.domain.model.Transaction;
import org.zairex_code.domain.port.out.TransactionCommandPort;

@ApplicationScoped
public class MongoTransactionCommandAdapter implements TransactionCommandPort {

    private static final String TRANSACTIONS_COLLECTION = "transactions";

    private final MongoCollection<Document> transactions;
    private final MongoTransactionContext transactionContext;

    @Inject
    public MongoTransactionCommandAdapter(MongoClient mongoClient,
                                          MongoTransactionContext transactionContext,
                                          @ConfigProperty(name = "quarkus.mongodb.database") String databaseName) {
        this.transactions = mongoClient.getDatabase(databaseName).getCollection(TRANSACTIONS_COLLECTION);
        this.transactionContext = transactionContext;
    }

    @Override
    public Transaction save(Transaction transaction) {
        Document document = MongoDocumentMapper.toDocument(transaction);
        transactions.replaceOne(transactionContext.current(),
                Filters.eq("_id", transaction.getId()),
                document,
                new ReplaceOptions().upsert(true));
        return transaction;
    }
}
