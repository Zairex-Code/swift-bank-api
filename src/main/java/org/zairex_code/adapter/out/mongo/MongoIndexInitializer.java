package org.zairex_code.adapter.out.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class MongoIndexInitializer {

    private static final Logger LOG = Logger.getLogger(MongoIndexInitializer.class);

    private final MongoClient mongoClient;
    private final String databaseName;

    @Inject
    public MongoIndexInitializer(MongoClient mongoClient,
                                 @ConfigProperty(name = "quarkus.mongodb.database") String databaseName) {
        this.mongoClient = mongoClient;
        this.databaseName = databaseName;
    }

    void onStart(@Observes StartupEvent event) {
        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            database.getCollection("accounts").createIndex(
                    Indexes.ascending("accountNumber"),
                    new IndexOptions().unique(true));
            database.getCollection("transactions").createIndex(Indexes.descending("timestamp"));
            LOG.info("MongoDB indexes ensured for accounts and transactions");
        } catch (RuntimeException exception) {
            LOG.warnv("Could not ensure MongoDB indexes: {0}", exception.getMessage());
        }
    }
}
