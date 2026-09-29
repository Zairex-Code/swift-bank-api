package org.zairex_code.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank(message = "sourceAccountId is required") String sourceAccountId,
        @NotBlank(message = "targetAccountId is required") String targetAccountId,
        @NotNull(message = "amount is required") @Positive(message = "amount must be positive") BigDecimal amount) {
}
