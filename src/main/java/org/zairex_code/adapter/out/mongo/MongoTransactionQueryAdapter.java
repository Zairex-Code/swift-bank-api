package org.zairex_code.adapter.out.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.zairex_code.domain.model.Transaction;
import org.zairex_code.domain.port.out.TransactionQueryPort;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class MongoTransactionQueryAdapter implements TransactionQueryPort {

    private static final String TRANSACTIONS_COLLECTION = "transactions";

    private final MongoCollection<Document> transactions;

    @Inject
    public MongoTransactionQueryAdapter(MongoClient mongoClient,
                                        @ConfigProperty(name = "quarkus.mongodb.database") String databaseName) {
        this.transactions = mongoClient.getDatabase(databaseName).getCollection(TRANSACTIONS_COLLECTION);
    }

    @Override
    public List<Transaction> findAll() {
        List<Transaction> result = new ArrayList<>();
        for (Document document : transactions.find().sort(Sorts.descending("timestamp"))) {
            result.add(MongoDocumentMapper.toTransaction(document));
        }
        return result;
    }
}
