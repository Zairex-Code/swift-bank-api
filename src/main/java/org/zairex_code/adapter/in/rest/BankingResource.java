package org.zairex_code.adapter.in.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.zairex_code.adapter.in.rest.dto.AccountRequest;
import org.zairex_code.adapter.in.rest.dto.AccountResponse;
import org.zairex_code.adapter.in.rest.dto.DepositRequest;
import org.zairex_code.adapter.in.rest.dto.TransactionResponse;
import org.zairex_code.adapter.in.rest.dto.TransferRequest;
import org.zairex_code.adapter.in.rest.mapper.AccountRestMapper;
import org.zairex_code.adapter.in.rest.mapper.TransactionRestMapper;
import org.zairex_code.domain.model.Account;
import org.zairex_code.domain.port.in.command.AccountCommandUseCase;
import org.zairex_code.domain.port.in.command.TransactionCommandUseCase;
import org.zairex_code.domain.port.in.query.AccountQueryUseCase;

import java.util.List;

@Path("/api/v1/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BankingResource {

    private final AccountCommandUseCase accountCommandUseCase;
    private final AccountQueryUseCase accountQueryUseCase;
    private final TransactionCommandUseCase transactionCommandUseCase;
    private final AccountRestMapper accountRestMapper;
    private final TransactionRestMapper transactionRestMapper;

    @Inject
    public BankingResource(AccountCommandUseCase accountCommandUseCase,
                           AccountQueryUseCase accountQueryUseCase,
                           TransactionCommandUseCase transactionCommandUseCase,
                           AccountRestMapper accountRestMapper,
                           TransactionRestMapper transactionRestMapper) {
        this.accountCommandUseCase = accountCommandUseCase;
        this.accountQueryUseCase = accountQueryUseCase;
        this.transactionCommandUseCase = transactionCommandUseCase;
        this.accountRestMapper = accountRestMapper;
        this.transactionRestMapper = transactionRestMapper;
    }

    @POST
    public Response createAccount(@Valid AccountRequest request) {
        Account account = accountCommandUseCase.createAccount(accountRestMapper.toDomain(request));
        return Response.status(Response.Status.CREATED)
                .entity(accountRestMapper.toResponse(account))
                .build();
    }

    @GET
    @Path("/{id}")
    public AccountResponse getAccount(@PathParam("id") String id) {
        return accountRestMapper.toResponse(accountQueryUseCase.getAccountById(id));
    }

    @GET
    public List<AccountResponse> listAccounts() {
        return accountQueryUseCase.getAllAccounts().stream()
                .map(accountRestMapper::toResponse)
                .toList();
    }

    @POST
    @Path("/{id}/deposits")
    public TransactionResponse deposit(@PathParam("id") String id, @Valid DepositRequest request) {
        return transactionRestMapper.toResponse(
                transactionCommandUseCase.deposit(id, request.amount()));
    }

    @POST
    @Path("/transfers")
    public TransactionResponse transfer(@Valid TransferRequest request) {
        return transactionRestMapper.toResponse(
                transactionCommandUseCase.transfer(
                        request.sourceAccountId(),
                        request.targetAccountId(),
                        request.amount()));
    }
}
