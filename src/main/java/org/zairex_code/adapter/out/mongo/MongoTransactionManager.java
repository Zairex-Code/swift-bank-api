package org.zairex_code.adapter.out.mongo;

import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.zairex_code.domain.port.out.TransactionManagerPort;

import java.util.function.Supplier;

@ApplicationScoped
public class MongoTransactionManager implements TransactionManagerPort {

    private static final Logger LOG = Logger.getLogger(MongoTransactionManager.class);

    private static final TransactionOptions TRANSACTION_OPTIONS =
            TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build();

    private final MongoClient mongoClient;
    private final MongoTransactionContext transactionContext;

    @Inject
    public MongoTransactionManager(MongoClient mongoClient, MongoTransactionContext transactionContext) {
        this.mongoClient = mongoClient;
        this.transactionContext = transactionContext;
    }

    @Override
    public <T> T execute(Supplier<T> operation) {
        try (ClientSession session = mongoClient.startSession()) {
            transactionContext.bind(session);
            try {
                session.startTransaction(TRANSACTION_OPTIONS);
                T result = operation.get();
                session.commitTransaction();
                return result;
            } catch (RuntimeException exception) {
                session.abortTransaction();
                LOG.warnv("Mongo transaction aborted: {0}", exception.getMessage());
                throw exception;
            } finally {
                transactionContext.clear();
            }
        }
    }
}
