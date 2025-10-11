package com.bootcamp.myproject.application.restrepository;

import com.bootcamp.myproject.application.model.TypeDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeDocumentRepository extends ReactiveMongoRepository<TypeDocument, String> {
}
