package org.zairex_code.adapter.in.rest.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.zairex_code.adapter.in.rest.dto.AccountRequest;
import org.zairex_code.adapter.in.rest.dto.AccountResponse;
import org.zairex_code.domain.model.Account;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AccountRestMapperTest {

    private final AccountRestMapper mapper = Mappers.getMapper(AccountRestMapper.class);

    @Test
    void toDomainMapsInitialBalanceIntoBalanceAndIgnoresId() {
        AccountRequest request = new AccountRequest("ACC-1", "Alice", new BigDecimal("25.00"));

        Account account = mapper.toDomain(request);

        assertEquals("ACC-1", account.getAccountNumber());
        assertEquals("Alice", account.getHolderName());
        assertEquals(new BigDecimal("25.00"), account.getBalance());
        assertNull(account.getId());
    }

    @Test
    void toResponseMapsAllFields() {
        Account account = Account.builder()
                .id("a1")
                .accountNumber("ACC-1")
                .holderName("Alice")
                .balance(new BigDecimal("25.00"))
                .build();

        AccountResponse response = mapper.toResponse(account);

        assertEquals("a1", response.id());
        assertEquals("ACC-1", response.accountNumber());
        assertEquals("Alice", response.holderName());
        assertEquals(new BigDecimal("25.00"), response.balance());
    }
}
