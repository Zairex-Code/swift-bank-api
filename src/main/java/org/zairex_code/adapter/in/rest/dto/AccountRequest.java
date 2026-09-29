package org.zairex_code.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record AccountRequest(
        @NotBlank(message = "accountNumber is required") String accountNumber,
        @NotBlank(message = "holderName is required") String holderName,
        @PositiveOrZero(message = "initialBalance must be zero or positive") BigDecimal initialBalance) {
}
