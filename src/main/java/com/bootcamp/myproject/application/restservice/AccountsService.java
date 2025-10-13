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

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import static com.bootcamp.myproject.application.model.CustomerProfile.PYME;
import static com.bootcamp.myproject.application.model.CustomerProfile.VIP;


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
    private final ParameterService parameterService;

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

                                    String key = "personal".equalsIgnoreCase(typeCustomer.getNameCustomer())
                                            ? "MIN_OPENING_AMOUNT_PERSONAL"
                                            : "MIN_OPENING_AMOUNT_EMPRESARIAL";

                                    return parameterService.getDouble(key)
                                                    .flatMap(minAmount -> {
                                                        if (account.getBalance() < minAmount) {
                                                            return Mono.error(new BusinessException(
                                                                    String.format("Monto inicial debe ser al menos %.2f", minAmount)
                                                            ));
                                                        }

                                                        if ((customer.getProfile() == VIP || customer.getProfile() == PYME) && !customer.isHasCreditCard()) {
                                                            return Mono.error(new BusinessException("El cliente necesita tarjeta de crédito para este tipo de cuenta"));
                                                        }

                                                        log.info("Validando reglas: TipoCliente={}, TipoCuenta={}", typeCustomerName, typeAccountName);

                                                        // Inicializar reglas de transacciones y comisión según tipo de cuenta
                                                        switch (typeAccountName.toLowerCase()) {
                                                            case "ahorro" -> {
                                                                account.setMaxFreeTransactions(5);
                                                                account.setTransactionCommission(BigDecimal.valueOf(1.00));
                                                            }
                                                            case "cuenta corriente" -> {
                                                                account.setMaxFreeTransactions(10);
                                                                account.setTransactionCommission(BigDecimal.valueOf(0.50));
                                                                account.setMinMonthlyAverage(BigDecimal.valueOf(500.00)); // 💡 ejemplo
                                                                account.setAccumulatedDailyBalance(BigDecimal.ZERO);
                                                                account.setDaysAccumulated(0);
                                                            }
                                                            case "plazo fijo" -> {
                                                                account.setMaxFreeTransactions(0);
                                                                account.setTransactionCommission(BigDecimal.ZERO);
                                                            }
                                                            default -> {
                                                                return Mono.error(new BusinessException("Tipo de cuenta no válido"));
                                                            }
                                                        }
                                                        account.setCurrentTransactions(0);

                                                        // Validación común para todos los tipos de cliente
                                                        if ("cuenta corriente".equalsIgnoreCase(typeAccountName) && !customer.isHasCreditCard()) {
                                                            return Mono.error(new BusinessException(
                                                                    "El cliente debe tener una tarjeta de crédito para abrir una cuenta corriente"
                                                            ));
                                                        }
                                                        if ("personal".equalsIgnoreCase(typeCustomerName)) {
                                                            return validatePersonalCustomer(account, typeAccountName);
                                                        } else if ("empresarial".equalsIgnoreCase(typeCustomerName)) {
                                                            return validateBusinessCustomer(account, typeAccountName);
                                                        } else {
                                                            return Mono.error(new BusinessException("Tipo de cliente no válido"));
                                                        }

                                                    });
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

     //transacciones
    public Mono<Accounts> processTransaction(String numAccount, Double amount, String type) {
        return accountsRepository.findByNumAccount(numAccount)
                .switchIfEmpty(Mono.error(new BusinessException("Cuenta no encontrada")))
                .flatMap(account -> {
                    int newTransactionCount = account.getCurrentTransactions() + 1;

                    // Verificar si se supera el límite de transacciones gratuitas
                    if (newTransactionCount > account.getMaxFreeTransactions()) {
                        BigDecimal commission = Optional.ofNullable(account.getTransactionCommission()).orElse(BigDecimal.ZERO);

                        account.setBalance(account.getBalance() - commission.doubleValue());
                        log.info("Aplicando comisión por transacción: {}", commission);
                    }

                    // Actualizar balance según tipo de transacción
                    if ("deposito".equalsIgnoreCase(type)) {
                        account.setBalance(account.getBalance() + amount.doubleValue());
                    } else if ("retiro".equalsIgnoreCase(type)) {
                        if (account.getBalance() < amount.doubleValue()) {
                            return Mono.error(new BusinessException("Saldo insuficiente"));
                        }
                        account.setBalance(account.getBalance() - amount.doubleValue());
                    } else {
                        return Mono.error(new BusinessException("Tipo de transacción no válido"));
                    }

                    account.setCurrentTransactions(newTransactionCount);
                    account.getMovements().add(String.format("%s: %.2f", type, amount));

                    return accountsRepository.save(account);
                });
    }

    public Mono<Void> updateDailyAverageBalances() {
        return accountsRepository.findAll()
                .flatMap(account ->
                        typeAccountRepository.findById(account.getIdTypeAccount())
                                .switchIfEmpty(Mono.error(new BusinessException("Tipo de cuenta no encontrado")))
                                .flatMap(typeAccount -> {
                                    String typeName = typeAccount.getNameAccount();

                                    // Solo aplica promedio diario a ciertos tipos de cuenta
                                    if ("ahorro".equalsIgnoreCase(typeName) ||
                                            "cuenta corriente".equalsIgnoreCase(typeName)) {

                                        BigDecimal balance = BigDecimal.valueOf(account.getBalance());

                                        // Evitar nulos
                                        if (account.getAccumulatedDailyBalance() == null)
                                            account.setAccumulatedDailyBalance(BigDecimal.ZERO);
                                        if (account.getDaysAccumulated() == 0)
                                            account.setDaysAccumulated(0);

                                        // Acumular el saldo diario
                                        account.setAccumulatedDailyBalance(
                                                account.getAccumulatedDailyBalance().add(balance)
                                        );
                                        account.setDaysAccumulated(account.getDaysAccumulated() + 1);

                                        log.info("Actualizado promedio diario -> Cuenta: {}, Tipo: {}, Saldo acumulado: {}, Días: {}",
                                                account.getNumAccount(),
                                                typeName,
                                                account.getAccumulatedDailyBalance(),
                                                account.getDaysAccumulated());

                                        return accountsRepository.save(account);
                                    }

                                    // Si no es tipo ahorro o corriente, no hace nada
                                    return Mono.empty();
                                })
                )
                .then();
    }

    //transferencias entre cuentas
    public Mono<Void> transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new BusinessException("El monto de transferencia debe ser mayor a 0"));
        }

        // Buscar ambas cuentas
        Mono<Accounts> fromAccountMono = accountsRepository.findByNumAccount(fromAccountNumber)
                .switchIfEmpty(Mono.error(new BusinessException("Cuenta origen no encontrada")));

        Mono<Accounts> toAccountMono = accountsRepository.findByNumAccount(toAccountNumber)
                .switchIfEmpty(Mono.error(new BusinessException("Cuenta destino no encontrada")));

        //Ejecutar transferencia
        return Mono.zip(fromAccountMono, toAccountMono)
                .flatMap(tuple -> {
                    Accounts from = tuple.getT1();
                    Accounts to = tuple.getT2();

                    if (from.getBalance() < amount.doubleValue()) {
                        return Mono.error(new BusinessException("Fondos insuficientes en cuenta origen"));
                    }

                    boolean sameCustomer = from.getIdCustomer().equals(to.getIdCustomer());
                    BigDecimal commission = BigDecimal.ZERO;

                    // Validar si es a terceros
                    if (!sameCustomer) {
                        // Comisión fija
                        commission = BigDecimal.valueOf(1.00);
                    }

                    BigDecimal totalDebit = amount.add(commission);

                    if (from.getBalance() < totalDebit.doubleValue()) {
                        return Mono.error(new BusinessException("Saldo insuficiente para cubrir monto y comisión"));
                    }

                    //Actualizar saldos
                    from.setBalance(from.getBalance() - totalDebit.doubleValue());
                    to.setBalance(to.getBalance() + amount.doubleValue());

                    // Registrar movimiento
                    String movementDesc = sameCustomer
                            ? String.format("Transferencia interna de S/ %.2f a %s", amount, to.getNumAccount())
                            : String.format("Transferencia a tercero de S/ %.2f a %s (Comisión S/ %.2f)", amount, to.getNumAccount(), commission);

                    if (from.getMovements() == null) from.setMovements(new ArrayList<>());
                    if (to.getMovements() == null) to.setMovements(new ArrayList<>());

                    from.getMovements().add("DEBITO: " + movementDesc);
                    to.getMovements().add("CREDITO: " + movementDesc);

                    log.info("{}", movementDesc);

                    //Guardar ambas cuentas
                    return accountsRepository.save(from)
                            .then(accountsRepository.save(to))
                            .then();
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

