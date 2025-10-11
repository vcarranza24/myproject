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


    Mono<Boolean> existsByNumAccount(String numAccount);

    Mono<Accounts> findByNumAccount(String numAccount);
    // Buscar cuentas activas por documento
    Flux<Accounts> findByNumDocumentAndState(String numDocument, int state);
}
