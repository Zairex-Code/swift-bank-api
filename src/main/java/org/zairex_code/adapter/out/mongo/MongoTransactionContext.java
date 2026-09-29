package org.zairex_code.adapter.out.mongo;

import com.mongodb.client.ClientSession;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MongoTransactionContext {

    private final ThreadLocal<ClientSession> currentSession = new ThreadLocal<>();

    public void bind(ClientSession session) {
        currentSession.set(session);
    }

    public ClientSession current() {
        return currentSession.get();
    }

    public void clear() {
        currentSession.remove();
    }
}
