package com.bootcamp.myproject.application.restrepository;

import com.bootcamp.myproject.application.model.Credits;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CreditsRepository extends ReactiveMongoRepository<Credits, String> {

    // Buscar créditos activos por documento
    Flux<Credits> findByNumDocumentAndState(String numDocument, int state);

    Mono<Credits> findByNumCardAndState(String numCard, int state);


    Mono<Boolean> existsByNumCard(String numCard);
}
