package com.bootcamp.myproject.application.restrepository;

import com.bootcamp.myproject.application.model.Accounts;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AccountsRepository extends ReactiveMongoRepository<Accounts, String> {

    // buscar cuentas activas por cliente y tipo
    Flux<Accounts> findByNumDocumentAndIdTypeAccountAndState(String numDocument, String idTypeAccount, int state);

    // validar si existe la cuenta del cliente
    Mono<Boolean> existsByNumAccount(String numAccount);

    // buscar número del cliente
    Mono<Accounts> findByNumAccount(String numAccount);

    Mono<Accounts> findByIdTypeAccount(String idTypeAccount);
    // Buscar cuentas activas por documento
    Flux<Accounts> findByNumDocumentAndState(String numDocument, int state);
}
