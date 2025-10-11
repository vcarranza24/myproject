package com.bootcamp.myproject.application.restcontroller;


import com.bootcamp.myproject.application.model.Movement;
import com.bootcamp.myproject.application.restservice.MovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/movements")
@RequiredArgsConstructor
public class MovementsController {

    private final MovementService movementService;


    @GetMapping("/{numDocument}")
    public Flux<Movement> getCustomerMovements(@PathVariable String numDocument) {
        return movementService.getMovementsByCustomer(numDocument);
    }

    @GetMapping("/product/{productNumber}")
    public Flux<Movement> getProductMovements(@PathVariable String productNumber) {
        return movementService.getMovementsByProduct(productNumber);
    }
}
