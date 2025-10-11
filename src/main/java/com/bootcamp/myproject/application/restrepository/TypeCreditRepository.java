package com.bootcamp.myproject.application.restrepository;

import com.bootcamp.myproject.application.model.TypeCredit;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeCreditRepository  extends ReactiveMongoRepository<TypeCredit, String> {
}
