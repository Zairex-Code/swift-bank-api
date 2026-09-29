package org.zairex_code.adapter.in.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DepositRequest(
        @NotNull(message = "amount is required") @Positive(message = "amount must be positive") BigDecimal amount) {
}
