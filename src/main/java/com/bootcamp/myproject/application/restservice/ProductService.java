package com.bootcamp.myproject.application.restservice;

import com.bootcamp.myproject.application.model.Product;
import com.bootcamp.myproject.application.restrepository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Flux<Product> getProductsByCategory(String name) {
        return productRepository.findByCategoryProduct_Name(name);
    }

    public Flux<Product> getProductsByAccount(String nameAccount) {
        return productRepository.findByCategoryProduct_Accounts_NameAccount(nameAccount);
    }

    public Flux<Product> getProductsByCredit(String nameCredit) {
        return productRepository.findByCategoryProduct_Credits_NameCredit(nameCredit);
    }
}
