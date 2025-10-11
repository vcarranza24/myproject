package com.bootcamp.myproject.application.restrepository;

import com.bootcamp.myproject.application.model.Customer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerRepository extends ReactiveMongoRepository<Customer, String> {
    Mono<Customer> findByNumDocument(String numDocument);
}
