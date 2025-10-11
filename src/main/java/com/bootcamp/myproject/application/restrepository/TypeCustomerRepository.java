package com.bootcamp.myproject.application.restrepository;

import com.bootcamp.myproject.application.model.TypeCustomer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeCustomerRepository extends ReactiveMongoRepository<TypeCustomer, String> {
}
