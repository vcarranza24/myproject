package com.bootcamp.myproject.application.restcontroller;

import com.bootcamp.myproject.application.model.Product;
import com.bootcamp.myproject.application.restservice.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "products", description = "Consulta de productos")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/category/{name}")
    @Operation(summary = "Consultar productos por categoria")
    public Flux<Product> getByCategory(@Parameter(description = "categoria", required = true) @PathVariable String name) {
        return productService.getProductsByCategory(name);
    }

    @GetMapping("/account/{name}")
    @Operation(summary = "Consultar cuentas")
    public Flux<Product> getByAccount(@Parameter(description = "tipo de cuenta", required = true) @PathVariable String name) {
        return productService.getProductsByAccount(name);
    }

    @GetMapping("/credit/{name}")
    @Operation(summary = "Consultar creditos")
    public Flux<Product> getByCredit(@Parameter(description = "tipo de creditos", required = true) @PathVariable String name) {
        return productService.getProductsByCredit(name);
    }

}
