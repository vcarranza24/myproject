package com.bootcamp.myproject.application.restcontroller;

import com.bootcamp.myproject.application.model.Credits;
import com.bootcamp.myproject.application.restservice.CreditService;
import com.bootcamp.myproject.application.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/credits")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;

    @PostMapping
    public Mono<ResponseEntity<Credits>> createCredit(@RequestBody Credits credit) {
        return creditService.createCredit(credit)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @PostMapping("/{numCard}/pay")
    public Mono<ResponseEntity<ApiResponse<Void>>> payCredit(
            @PathVariable String numCard,
            @RequestParam Double amount) {
        return creditService.payCredit(numCard, amount)
                .then(Mono.just(
                        ResponseEntity.ok(ApiResponse.noContent("Se realizó el pago con éxito"))
                ));
    }

    @PostMapping("/{numCard}/consume")
    public Mono<ResponseEntity<ApiResponse<Void>>> registerConsumption(
            @PathVariable String numCard,
            @RequestParam Double amount) {
        return creditService.registerConsumption(numCard, amount)
                .then(Mono.just(
                        ResponseEntity.ok(ApiResponse.noContent("Se realizó el consumo con éxito"))
                ));
    }

}
