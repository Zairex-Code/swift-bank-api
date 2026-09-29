package org.zairex_code.adapter.in.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.zairex_code.adapter.in.rest.dto.TransactionResponse;
import org.zairex_code.adapter.in.rest.mapper.TransactionRestMapper;
import org.zairex_code.domain.port.in.query.TransactionQueryUseCase;

import java.util.List;

@Path("/api/v1/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionResource {

    private final TransactionQueryUseCase transactionQueryUseCase;
    private final TransactionRestMapper transactionRestMapper;

    @Inject
    public TransactionResource(TransactionQueryUseCase transactionQueryUseCase,
                               TransactionRestMapper transactionRestMapper) {
        this.transactionQueryUseCase = transactionQueryUseCase;
        this.transactionRestMapper = transactionRestMapper;
    }

    @GET
    public List<TransactionResponse> listTransactions() {
        return transactionQueryUseCase.getAllTransactions().stream()
                .map(transactionRestMapper::toResponse)
                .toList();
    }
}
