package com.bootcamp.myproject.application.restrepository;

import com.bootcamp.myproject.application.model.Movement;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MovementsRepository extends ReactiveMongoRepository<Movement, String> {

    Flux<Movement> findByNumDocument(String numDocument);

    Flux<Movement> findByNumProduct(String numProduct);

    Flux<Movement>findByNumDocumentAndDateBetween(String numDocument, String startDate, String endDate);

    Flux<Movement>findByNumProductAndDateBetween(String numProduct, String startDate, String endDate);

}
