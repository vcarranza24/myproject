package com.bootcamp.myproject.application.restcontroller;


import com.bootcamp.myproject.application.model.Movement;
import com.bootcamp.myproject.application.restservice.MovementService;
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
@RequestMapping("/movements")
@RequiredArgsConstructor
@Tag(name = "movements", description = "Consulta de movimientos")
public class MovementsController {

    private final MovementService movementService;


    @GetMapping("/{numDocument}")
    @Operation(summary = "Consultar movimientos por numero de documento")
    public Flux<Movement> getCustomerMovements(@Parameter(description = "Número de documento del cliente", required = true)
                                               @PathVariable String numDocument) {
        return movementService.getMovementsByCustomer(numDocument);
    }

    @GetMapping("/product/{productNumber}")
    @Operation(summary = "Consultar movimientos por numero de producto")
    public Flux<Movement> getProductMovements(@Parameter(description = "Número de producto", required = true)
                                              @PathVariable String productNumber) {
        return movementService.getMovementsByProduct(productNumber);
    }
}
