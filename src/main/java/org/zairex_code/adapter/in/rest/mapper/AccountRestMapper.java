package org.zairex_code.adapter.in.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.zairex_code.adapter.in.rest.dto.AccountRequest;
import org.zairex_code.adapter.in.rest.dto.AccountResponse;
import org.zairex_code.domain.model.Account;

@Mapper(componentModel = "cdi")
public interface AccountRestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "balance", source = "initialBalance")
    Account toDomain(AccountRequest request);

    AccountResponse toResponse(Account account);
}
