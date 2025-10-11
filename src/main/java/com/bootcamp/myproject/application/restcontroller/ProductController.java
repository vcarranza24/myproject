package com.bootcamp.myproject.application.restcontroller;

import com.bootcamp.myproject.application.model.Product;
import com.bootcamp.myproject.application.restrepository.ProductRepository;
import com.bootcamp.myproject.application.restservice.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService  productService;

    @GetMapping("/category/{name}")
    public Flux<Product> getByCategory(@PathVariable String name) {
        return productService.getProductsByCategory(name);
    }

    @GetMapping("/account/{name}")
    public Flux<Product> getByAccount(@PathVariable String name) {
        return productService.getProductsByAccount(name);
    }

    @GetMapping("/credit/{name}")
    public Flux<Product> getByCredit(@PathVariable String name) {
        return productService.getProductsByCredit(name);
    }

}
