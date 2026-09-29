package org.zairex_code.adapter.in.rest.dto;

import java.math.BigDecimal;

public record AccountResponse(
        String id,
        String accountNumber,
        String holderName,
        BigDecimal balance) {
}
