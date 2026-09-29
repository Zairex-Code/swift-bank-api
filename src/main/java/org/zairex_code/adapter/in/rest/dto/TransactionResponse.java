package org.zairex_code.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        String id,
        String sourceAccountId,
        String targetAccountId,
        BigDecimal amount,
        String status,
        String rejectionReason,
        LocalDateTime timestamp) {
}
