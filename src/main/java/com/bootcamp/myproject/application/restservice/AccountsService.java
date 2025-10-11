package com.bootcamp.myproject.application.restservice;

import com.bootcamp.myproject.application.model.Accounts;
import com.bootcamp.myproject.application.model.TypeAccount;
import com.bootcamp.myproject.application.model.TypeCustomer;
import com.bootcamp.myproject.application.restrepository.*;
import com.bootcamp.myproject.application.util.BusinessException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;


@Service
@Slf4j
@RequiredArgsConstructor
public class AccountsService {

    private final AccountsRepository accountsRepository;
    private final TypeCustomerRepository typeCustomerRepository;
    private final CustomerRepository customerRepository;
    private final TypeAccountRepository typeAccountRepository;
    private final MovementService movementService;
    private final TypeMovementRepository typeMovementRepository;


    public Mono<Accounts> createAccount(Accounts account) {
        log.info("Creando cuenta -> Cliente: {}, TipoCuenta: {}", account.getIdCustomer(), account.getIdTypeAccount());

        return customerRepository.findById(account.getIdCustomer())
                .switchIfEmpty(Mono.error(new BusinessException("Cliente no encontrado")))
                .flatMap(customer ->

                        Mono.zip(
                                        typeCustomerRepository.findById(customer.getIdTypeCustomer())
                                                .switchIfEmpty(Mono.error(new BusinessException("Tipo de cliente no encontrado"))),
                                        typeAccountRepository.findById(account.getIdTypeAccount())
                                                .switchIfEmpty(Mono.error(new BusinessException("Tipo de cuenta no encontrado")))
                                )
                                .flatMap(tuple -> {
                                    TypeCustomer typeCustomer = tuple.getT1();
                                    TypeAccount typeAccount = tuple.getT2();
                                    log.info("Validando reglas: TipoCliente={}, TipoCuenta={}", typeCustomer.getNameCustomer(), typeAccount.getNameAccount());
                                    String typeCustomerName = typeCustomer.getNameCustomer();
                                    String typeAccountName = typeAccount.getNameAccount();

                                    if ("personal".equalsIgnoreCase(typeCustomerName)) {
                                        return validatePersonalCustomer(account, typeAccountName);
                                    } else if ("empresarial".equalsIgnoreCase(typeCustomerName)) {
                                        return validateBusinessCustomer(account, typeAccountName);
                                    } else {
                                        return Mono.error(new BusinessException("Tipo de cliente no válido"));
                                    }
                                })
                );
    }

    //Deposito a sus cuentas bancarias
    @CircuitBreaker(name = "depositAccountService", fallbackMethod = "fallbackDepositAccount")
    @Retry(name = "depositAccountService")
      public Mono<Accounts> depositAccount(String numAccount, Double amount) {
        log.info("Realizando depósito -> Cuenta: {}, Monto: {}", numAccount, amount);

        if (amount == null || amount <= 0) {
            return Mono.error(new BusinessException("El monto del depósito debe ser mayor a cero"));
        }

        return accountsRepository.existsByNumAccount(numAccount)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new BusinessException("Cuenta no encontrada"));
                    }
                    return accountsRepository.findByNumAccount(numAccount)
                            .flatMap(account -> {
                                if (account.getState() != 1) {
                                    return Mono.error(new BusinessException("La cuenta no está activa"));
                                }
                                account.setBalance(account.getBalance() + amount);

                                // Registrar movimiento tipo deposito
                                return typeMovementRepository.findByNameMovementIgnoreCase("deposito")
                                        .timeout(Duration.ofSeconds(3))
                                        .flatMap(typeMovement ->
                                                movementService.createMovement(
                                                        account.getId(),          // idAccount
                                                        null,                     // idCredit
                                                        typeMovement.getId(),     // idTypeMovement
                                                        account.getNumDocument(),
                                                        account.getNumAccount(),
                                                        LocalDateTime.now().toString(),
                                                        amount,
                                                        "Depósito a cuenta bancaria"
                                                )
                                        )
                                        .then(accountsRepository.save(account));
                            });


                })
                .onErrorResume(ex -> fallbackDepositAccount(numAccount, amount, ex));
    }

    //Retiro de sus cuentas bancarias
    public Mono<Accounts> withdrawAccount(String numAccount, Double amount) {
        log.info("Realizando retiro -> Cuenta: {}, Monto: {}", numAccount, amount);

        if (amount == null || amount <= 0) {
            return Mono.error(new BusinessException("El monto del retiro debe ser mayor a cero"));
        }

        return accountsRepository.findByNumAccount(numAccount)
                .switchIfEmpty(Mono.error(new BusinessException("Cuenta no encontrada")))
                .flatMap(account -> {
                    if (account.getState() != 1) {
                        return Mono.error(new BusinessException("La cuenta no está activa"));
                    }
                    if (account.getBalance() < amount) {
                        return Mono.error(new BusinessException("Fondos insuficientes para el retiro"));
                    }
                    account.setBalance(account.getBalance() - amount);

                    // Registrar movimiento tipo retiro
                    return typeMovementRepository.findByNameMovementIgnoreCase("retiro")
                            .flatMap(typeMovement ->
                                    movementService.createMovement(
                                            account.getId(),          // idAccount
                                            null,                     // idCredit
                                            typeMovement.getId(),     // idTypeMovement
                                            account.getNumDocument(),
                                            account.getNumAccount(),
                                            LocalDateTime.now().toString(),
                                            amount,
                                            "Retiro de cuenta bancaria"
                                    )
                            )
                            .then(accountsRepository.save(account));
                });

    }


    // -------- VALIDACIONES -------- //

    private Mono<Accounts> validatePersonalCustomer(Accounts account, String typeAccountName) {
        String numDocument = account.getNumDocument();
        String numAccount = account.getNumAccount();


        return accountsRepository.existsByNumAccount(numAccount)
                .flatMap(numAccountExists -> {
                            if (numAccountExists) {
                                return Mono.error(new BusinessException("El número de cuenta ya existe"));
                            }


        // Para cuentas a plazo fijo no hay límite en cantidad
        if (Objects.equals(typeAccountName, "plazo fijo")) {
            account.setBalance(0.0);
            account.setState(1);
            log.info("Datos del cliente: {}", account.toString());
            return accountsRepository.save(account)
                    .doOnSuccess(a -> log.info(" Cuenta personal creada a cuentas a plazo fijo: {}", a.getNumAccount()));
        }

        // Para ahorro y corriente solo puede existir 1 cuenta activa de ese tipo
        return accountsRepository
                .findByNumDocumentAndIdTypeAccountAndState(numDocument, account.getIdTypeAccount(), 1)
                .hasElements()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new BusinessException(
                                "El cliente personal ya tiene una cuenta de tipo " + typeAccountName));
                    }

                    // No puede tener titulares ni firmantes
                    if (account.getHeadlines() != null && !account.getHeadlines().isEmpty()) {
                        return Mono.error(new BusinessException(
                                "Un cliente personal no puede tener titulares"
                        ));
                    }
                    if (account.getSignatories() != null && !account.getSignatories().isEmpty()) {
                        return Mono.error(new BusinessException(
                                "Un cliente personal no puede tener firmantes"
                        ));
                    }
                    account.setBalance(0.0);
                    account.setState(1);
                    return accountsRepository.save(account);
                });

         });
    }

    private Mono<Accounts> validateBusinessCustomer(Accounts account, String typeAccountName) {
        String numAccount = account.getNumAccount();

        return accountsRepository.existsByNumAccount(numAccount)
                .flatMap(numAccountExists -> {
                    if (numAccountExists) {
                        return Mono.error(new BusinessException("El número de cuenta ya existe"));
                    }
        if ("ahorro".equalsIgnoreCase(typeAccountName) || "plazo fijo".equalsIgnoreCase(typeAccountName)) {
            return Mono.error(new BusinessException(
                    "Un cliente empresarial no puede tener cuentas de ahorro o plazo fijo"
            ));
        }

        //  Nueva validación: titulares y firmantes
        if (account.getHeadlines() == null || account.getHeadlines().isEmpty()) {
            return Mono.error(new BusinessException(
            "Una cuenta empresarial debe tener al menos un titular"
            ));
            }

        // Si la lista de firmantes es nula, inicializarla vacía
        if (account.getSignatories() == null) {
            account.setSignatories(new ArrayList<>());
                    }

        // Puede tener múltiples cuentas corrientes
        account.setState(1);
        account.setBalance(0.0);
        return accountsRepository.save(account)
                .doOnSuccess(a -> log.info("Cuenta empresarial creada: {}", a.getNumAccount()));

         });

    }


    private Mono<Accounts> fallbackDepositAccount(String numAccount, Double amount, Throwable ex) {
        log.error("Fallback activado para depósito en cuenta {} por: {}", numAccount, ex.toString());
        Accounts fallback = new Accounts();
        fallback.setNumAccount(numAccount);
        fallback.setBalance(0.0);
        fallback.setState(0);
        return Mono.just(fallback);
    }

}

