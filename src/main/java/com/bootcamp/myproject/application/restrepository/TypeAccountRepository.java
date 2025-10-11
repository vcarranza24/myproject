package com.bootcamp.myproject.application.restrepository;

import com.bootcamp.myproject.application.model.TypeAccount;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeAccountRepository extends ReactiveMongoRepository<TypeAccount,String> {
}
