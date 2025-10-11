package com.bootcamp.myproject.application.restrepository;

import com.bootcamp.myproject.application.model.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;


@Repository
public interface  ProductRepository extends ReactiveMongoRepository<Product, String> {

    // Busca por nombre de categoría (ej: "pasivo" o "activo")
    Flux<Product> findByCategoryProduct_Name(String name);

    // Busca productos que tengan una cuenta con nombre específico (ej: "ahorro")
    Flux<Product> findByCategoryProduct_Accounts_NameAccount(String nameAccount);

    // Busca productos que tengan un crédito con nombre específico (ej: "tarjeta de crédito")
    Flux<Product> findByCategoryProduct_Credits_NameCredit(String nameCredit);
}
