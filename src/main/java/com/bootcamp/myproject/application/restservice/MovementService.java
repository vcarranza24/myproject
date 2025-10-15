package com.bootcamp.myproject.application.restservice;

import com.bootcamp.myproject.application.model.Movement;
import com.bootcamp.myproject.application.restrepository.AccountsRepository;
import com.bootcamp.myproject.application.restrepository.MovementsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovementService {

    private final MovementsRepository movementsRepository;
    private final AccountsRepository accountsRepository;

    public Mono<Movement> createMovement(String idAccount, String idCredit, String idTypeMovement, String numDocument, String numProduct, String date, BigDecimal amount, String description) {
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
        return movementsRepository.findByNumDocument(numDocument).sort((m1, m2) -> m2.getDate().compareTo(m1.getDate())); // orden descendente
    }

    // Consultar movimientos de un producto específico
    public Flux<Movement> getMovementsByProduct(String numProduct) {
        return movementsRepository.findByNumProduct(numProduct).sort((m1, m2) -> m2.getDate().compareTo(m1.getDate()));
    }


    //comisiones cobradas por producto en un rango de fechas

   /* public Flux<CommissionReport> getCommissionReport(String productId, LocalDate from, LocalDate to) {
        return movementsRepository.findByProductIdAndDateBetween(productId, from, to)
                .filter(m -> m.getCommission() != null)
                .map(...);
    }*/

}

