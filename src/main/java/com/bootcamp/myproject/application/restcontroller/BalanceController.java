package com.bootcamp.myproject.application.restcontroller;

import com.bootcamp.myproject.application.restservice.BalanceService;
import com.bootcamp.myproject.application.util.BalanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/balance")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping("/{numDocument}")
    public Flux<BalanceResponse> getBalances(@PathVariable String numDocument) {
        return balanceService.getAvailableBalances(numDocument);
    }
}
