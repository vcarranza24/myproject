package com.bootcamp.myproject.application.restservice;

import com.bootcamp.myproject.application.model.Credits;
import com.bootcamp.myproject.application.model.Customer;
import com.bootcamp.myproject.application.restrepository.CreditsRepository;
import com.bootcamp.myproject.application.restrepository.CustomerRepository;
import com.bootcamp.myproject.application.restrepository.TypeCustomerRepository;
import com.bootcamp.myproject.application.restrepository.TypeMovementRepository;
import com.bootcamp.myproject.application.util.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreditService {

    private final CreditsRepository creditsRepository;
    private final CustomerRepository customerRepository;
    private final TypeCustomerRepository typeCustomerRepository;
    private final TypeMovementRepository typeMovementRepository;
    private final MovementService movementService;

    public Mono<Credits> createCredit(Credits credit) {
        return customerRepository.findByNumDocument(credit.getNumDocument())
                .switchIfEmpty(Mono.error(new BusinessException("Cliente no encontrado")))
                .flatMap(customer -> validateCreditRules(customer, credit))
                .flatMap(valid -> creditsRepository.save(credit));
    }

    private Mono<Boolean> validateCreditRules(Customer customer, Credits credit) {

        return creditsRepository.existsByNumCard(credit.getNumCard())
                .flatMap(numCardExists -> {
                    if (numCardExists) {
                        return Mono.error(new BusinessException("El número de tarjeta ya existe"));
                    }
        return typeCustomerRepository.findById(customer.getIdTypeCustomer())
                .flatMap(type -> {
                    if ("personal".equalsIgnoreCase(type.getNameCustomer())) {
                        return creditsRepository.findByNumDocumentAndState(customer.getNumDocument(), 1)
                                .count()
                                .flatMap(count -> {
                                    if (count >= 1) {
                                        return Mono.error(new BusinessException("El cliente personal solo puede tener un crédito activo"));
                                    }
                                    return Mono.just(true);
                                });
                    } else if ("empresarial".equalsIgnoreCase(type.getNameCustomer())) {
                        // Puede tener varios créditos — sin restricción
                        return Mono.just(true);
                    } else {
                        return Mono.error(new BusinessException("Tipo de cliente no válido"));
                    }
                });

          });

    }

    /**
     * Realizar el pago de un crédito.
     */
    public Mono<Credits> payCredit(String numCard, Double paymentAmount) {
        if (paymentAmount == null || paymentAmount <= 0) {
            return Mono.error(new BusinessException("El monto de pago debe ser mayor a 0"));
        }

        return creditsRepository.findByNumCardAndState(numCard, 1)
                .switchIfEmpty(Mono.error(new BusinessException("Crédito no encontrado o inactivo")))
                .flatMap(credit -> {
                    double currentBalance = credit.getLineUsed();
                    double newline = currentBalance - paymentAmount;

                    if (newline < 0) {
                        newline = 0.0;
                    }

                    // Actualiza el saldo
                    credit.setLineUsed(newline);
                    credit.setBalance(credit.getBalance() + paymentAmount);

                    // Registrar movimiento tipo pago
                    return typeMovementRepository.findByNameMovementIgnoreCase("pago")
                            .flatMap(typeMovement ->
                                    movementService.createMovement(
                                            null,          // idAccount
                                            credit.getId(),                     // idCredit
                                            typeMovement.getId(),     // idTypeMovement
                                            credit.getNumDocument(),
                                            credit.getNumCard(),
                                            LocalDateTime.now().toString(),
                                            paymentAmount,
                                            "Pago a tarjeta de crédito"
                                    )
                            )
                            .then(creditsRepository.save(credit));
                });


    }


    /**
     * Registrar un consumo de crédito.
     */
    public Mono<Credits> registerConsumption(String numCard, Double purchaseAmount) {
        if (purchaseAmount == null || purchaseAmount <= 0) {
            return Mono.error(new BusinessException("El monto del consumo debe ser mayor a 0"));
        }

        return creditsRepository.findByNumCardAndState(numCard, 1)
                .switchIfEmpty(Mono.error(new BusinessException("Tarjeta no encontrada o inactiva")))
                .flatMap(credit -> {
                    double limitCredit = credit.getLimitCredit() != null ? credit.getLimitCredit() : 0;
                    double lineUsed = credit.getLineUsed() != null ? credit.getLineUsed() : 0;
                    double balance = credit.getBalance() != null ? credit.getBalance() : (limitCredit - lineUsed);

                    if (purchaseAmount > balance) {
                        return Mono.error(new BusinessException("El monto excede el saldo disponible del crédito"));
                    }

                    credit.setLineUsed(lineUsed + purchaseAmount);
                    credit.setBalance(balance - purchaseAmount);

                    // Validar consistencia
                    if (Math.abs((credit.getLineUsed() + credit.getBalance()) - limitCredit) > 0.01) {
                        credit.setBalance(limitCredit - credit.getLineUsed());
                    }

                    // Registrar movimiento tipo consumo
                    return typeMovementRepository.findByNameMovementIgnoreCase("consumo")
                            .flatMap(typeMovement ->
                                    movementService.createMovement(
                                            null,          // idAccount
                                            credit.getId(),                     // idCredit
                                            typeMovement.getId(),     // idTypeMovement
                                            credit.getNumDocument(),
                                            credit.getNumCard(),
                                            LocalDateTime.now().toString(),
                                            purchaseAmount,
                                            "Consumo con tarjeta de crédito"
                                    )
                            )
                            .then(creditsRepository.save(credit));
                });

    }
}
