package com.bootcamp.myproject.application.restrepository;

import com.bootcamp.myproject.application.model.Parameter;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ParameterRepository extends ReactiveMongoRepository <Parameter, String> {

    Mono<Parameter> findByKey(String key);

}
