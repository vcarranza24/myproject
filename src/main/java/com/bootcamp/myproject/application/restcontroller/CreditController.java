package com.bootcamp.myproject.application.restcontroller;

import com.bootcamp.myproject.application.model.Credits;
import com.bootcamp.myproject.application.restservice.CreditService;
import com.bootcamp.myproject.application.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/credits")
@RequiredArgsConstructor
@Tag(name = "credits", description = "operaciones de créditos")
public class CreditController {

    private final CreditService creditService;

    @PostMapping
    @Operation(summary = "Creación de creditos")
    public Mono<ResponseEntity<Credits>> createCredit(@RequestBody Credits credit) {
        return creditService.createCredit(credit)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @PostMapping("/{numCard}/pay")
    @Operation(summary = "Pago de cuentas de creditos")
    public Mono<ResponseEntity<ApiResponse<Void>>> payCredit(
            @Parameter(description = "Número de tarjeta y monto", required = true)
            @PathVariable String numCard,
            @RequestParam Double amount) {
        return creditService.payCredit(numCard, amount)
                .then(Mono.just(
                        ResponseEntity.ok(ApiResponse.noContent("Se realizó el pago con éxito"))
                ));
    }

    @PostMapping("/{numCard}/consume")
    @Operation(summary = "Consumo de creditos")
    public Mono<ResponseEntity<ApiResponse<Void>>> registerConsumption(
            @Parameter(description = "Número de tarjeta y monto", required = true)
            @PathVariable String numCard,
            @RequestParam Double amount) {
        return creditService.registerConsumption(numCard, amount)
                .then(Mono.just(
                        ResponseEntity.ok(ApiResponse.noContent("Se realizó el consumo con éxito"))
                ));
    }

}
