package org.zairex_code.domain.port.out;

import java.util.function.Supplier;

public interface TransactionManagerPort {

    <T> T execute(Supplier<T> operation);
}
