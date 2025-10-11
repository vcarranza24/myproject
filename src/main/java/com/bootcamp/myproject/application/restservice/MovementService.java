package com.bootcamp.myproject.application.restservice;

import com.bootcamp.myproject.application.client.AccountClient;
import com.bootcamp.myproject.application.model.Movement;
import com.bootcamp.myproject.application.restrepository.MovementsRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@Service
@RequiredArgsConstructor
public class MovementService {

    private final MovementsRepository movementsRepository;


    public Mono<Movement> createMovement(String idAccount,
                                           String idCredit,
                                           String idTypeMovement,
                                           String numDocument,
                                           String numProduct,
                                           String date,
                                           Double amount,
                                           String description) {
        Movement movement = new Movement();
        movement.setIdAccount(idAccount);
        movement.setIdCredit(idCredit);
        movement.setIdTypeMovement(idTypeMovement);
        movement.setNumDocument(numDocument);
        movement.setNumProduct(numProduct);
        movement.setDate(date);
        movement.setAmount(amount);
        movement.setDescription(description);

        return movementsRepository.save(movement);
    }

    // Consultar movimientos por documento (todos los productos)
    public Flux<Movement> getMovementsByCustomer(String numDocument) {
        return movementsRepository.findByNumDocument(numDocument)
                .sort((m1, m2) -> m2.getDate().compareTo(m1.getDate())); // orden descendente
    }

    // Consultar movimientos de un producto específico
    public Flux<Movement> getMovementsByProduct(String numProduct) {
        return movementsRepository.findByNumProduct(numProduct)
                .sort((m1, m2) -> m2.getDate().compareTo(m1.getDate()));
    }
}

