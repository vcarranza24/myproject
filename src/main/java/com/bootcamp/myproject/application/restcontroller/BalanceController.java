package com.bootcamp.myproject.application.restcontroller;

import com.bootcamp.myproject.application.restservice.BalanceService;
import com.bootcamp.myproject.application.util.BalanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/balance")
@RequiredArgsConstructor
@Tag(name = "balance", description = "Consulta de saldos")
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping("/{numDocument}")
    @Operation(summary = "Consulta saldo disponibles")
    public Flux<BalanceResponse> getBalances(@Parameter(description = "Número de documento del cliente", required = true)
                                             @PathVariable String numDocument) {
        return balanceService.getAvailableBalances(numDocument);
    }
}
