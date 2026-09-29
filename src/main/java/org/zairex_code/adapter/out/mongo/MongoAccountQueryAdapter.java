package org.zairex_code.adapter.out.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.zairex_code.domain.model.Account;
import org.zairex_code.domain.port.out.AccountQueryPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MongoAccountQueryAdapter implements AccountQueryPort {

    private static final String ACCOUNTS_COLLECTION = "accounts";

    private final MongoCollection<Document> accounts;

    @Inject
    public MongoAccountQueryAdapter(MongoClient mongoClient,
                                    @ConfigProperty(name = "quarkus.mongodb.database") String databaseName) {
        this.accounts = mongoClient.getDatabase(databaseName).getCollection(ACCOUNTS_COLLECTION);
    }

    @Override
    public Optional<Account> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        Document document = accounts.find(Filters.eq("_id", id)).first();
        return Optional.ofNullable(document).map(MongoDocumentMapper::toAccount);
    }

    @Override
    public List<Account> findAll() {
        List<Account> result = new ArrayList<>();
        for (Document document : accounts.find()) {
            result.add(MongoDocumentMapper.toAccount(document));
        }
        return result;
    }
}
