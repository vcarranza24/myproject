package com.bootcamp.myproject.application.restcontroller;

import com.bootcamp.myproject.application.model.Accounts;
import com.bootcamp.myproject.application.restservice.AccountsService;
import com.bootcamp.myproject.application.util.ApiResponse;
import com.bootcamp.myproject.application.util.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountsController {

    private final AccountsService accountsService;

    @PostMapping
    public Mono<ResponseEntity<Accounts>> createAccount(@RequestBody Accounts account) {
        return accountsService.createAccount(account)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @PostMapping("/deposit")
    public Mono<ResponseEntity<ApiResponse<Void>>> depositAccount(@RequestParam String numAccount, @RequestParam Double amount) {
        return accountsService.depositAccount(numAccount, amount)
                .then(Mono.just(
                        ResponseEntity.ok(ApiResponse.noContent("Se realizó el deposito de: S/."+amount))
                ));
    }

    @PostMapping("/withdraw")
    public Mono<ResponseEntity<ApiResponse<Void>>> withdrawAccount(@RequestParam String numAccount, @RequestParam Double amount) {
        return accountsService.withdrawAccount(numAccount, amount)
                .then(Mono.just(
                        ResponseEntity.ok(ApiResponse.noContent("Se realizó el retiro de: S/."+amount))
                ));
    }


}
