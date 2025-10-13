package com.bootcamp.myproject.application.restcontroller;

import com.bootcamp.myproject.application.model.Accounts;
import com.bootcamp.myproject.application.restservice.AccountsService;
import com.bootcamp.myproject.application.util.ApiResponse;
import com.bootcamp.myproject.application.util.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "accounts", description = "Operaciones de cuentas")
public class AccountsController {

    private final AccountsService accountsService;

    @PostMapping
    @Operation(summary = "Crear cuentas")
    public Mono<ResponseEntity<Accounts>> createAccount(@RequestBody Accounts account) {
        return accountsService.createAccount(account)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @PostMapping("/deposit")
    @Operation(summary = "Deposito por numero de cuenta")
    public Mono<ResponseEntity<ApiResponse<Void>>> depositAccount( @Parameter(description = "Número de cuenta y monto", required = true)
                                                                   @RequestParam String numAccount, @RequestParam Double amount) {
        return accountsService.depositAccount(numAccount, amount)
                .then(Mono.just(
                        ResponseEntity.ok(ApiResponse.noContent("Se realizó el deposito de: S/."+amount))
                ));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Retiro por numero de cuenta")
    public Mono<ResponseEntity<ApiResponse<Void>>> withdrawAccount(@Parameter(description = "Número de cuenta y monto", required = true)
                                                                   @RequestParam String numAccount, @RequestParam Double amount) {
        return accountsService.withdrawAccount(numAccount, amount)
                .then(Mono.just(
                        ResponseEntity.ok(ApiResponse.noContent("Se realizó el retiro de: S/."+amount))
                ));
    }

    @PostMapping("/processTransaction")
    @Operation(summary = "transacciones")
    public Mono<ResponseEntity<ApiResponse<Void>>> processTransaction(@Parameter(description = "Número de cuenta , monto y tipo", required = true)
                                                                   @RequestParam String numAccount, @RequestParam Double amount, @RequestParam String type) {

        return accountsService.processTransaction(numAccount, amount, type)
                .then(Mono.just(
                        ResponseEntity.ok(ApiResponse.noContent("Se realizó la transferencia de: S/."+amount))
                ));
    }

    @PostMapping("/updateDailyAverageBalances")
    public Mono<ResponseEntity<String>> updateDailyBalances() {
        return accountsService.updateDailyAverageBalances()
                .then(Mono.just(ResponseEntity.ok("Balances diarios actualizados correctamente")));
    }


    @PostMapping("/transfer")
    @Operation(summary = "Transferencias entre cuentas del mismo banco")
    public Mono<ResponseEntity<ApiResponse<Void>>> transfer(
            @RequestParam String fromAccount,
            @RequestParam String toAccount,
            @RequestParam BigDecimal amount) {

        return accountsService.transfer(fromAccount, toAccount, amount)
                .then(Mono.just(ResponseEntity.ok(ApiResponse.noContent("Transferencia realizada correctamente"))));
    }



}
