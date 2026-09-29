package org.zairex_code.adapter.in.rest.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.zairex_code.adapter.in.rest.dto.TransactionResponse;
import org.zairex_code.domain.model.Transaction;
import org.zairex_code.domain.model.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionRestMapperTest {

    private final TransactionRestMapper mapper = Mappers.getMapper(TransactionRestMapper.class);

    @Test
    void toResponseMapsStatusToString() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 1, 1, 10, 0);
        Transaction transaction = Transaction.builder()
                .id("t1")
                .sourceAccountId("a1")
                .targetAccountId("a2")
                .amount(new BigDecimal("40.00"))
                .status(TransactionStatus.COMPLETED)
                .timestamp(timestamp)
                .build();

        TransactionResponse response = mapper.toResponse(transaction);

        assertEquals("t1", response.id());
        assertEquals("a1", response.sourceAccountId());
        assertEquals("a2", response.targetAccountId());
        assertEquals(new BigDecimal("40.00"), response.amount());
        assertEquals("COMPLETED", response.status());
        assertEquals(timestamp, response.timestamp());
    }
}
