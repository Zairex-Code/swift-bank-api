package org.zairex_code.adapter.in.rest.mapper;

import org.mapstruct.Mapper;
import org.zairex_code.adapter.in.rest.dto.TransactionResponse;
import org.zairex_code.domain.model.Transaction;

@Mapper(componentModel = "cdi")
public interface TransactionRestMapper {

    TransactionResponse toResponse(Transaction transaction);
}
