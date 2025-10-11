package com.bootcamp.myproject.application.restrepository;

import com.bootcamp.myproject.application.model.TypeMovement;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface TypeMovementRepository extends ReactiveMongoRepository<TypeMovement, String> {

    Mono<TypeMovement> findByNameMovementIgnoreCase(String nameMovement);
}
