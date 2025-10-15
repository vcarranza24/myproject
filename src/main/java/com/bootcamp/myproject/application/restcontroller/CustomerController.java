package com.bootcamp.myproject.application.restcontroller;

import com.bootcamp.myproject.application.restservice.CustomerService;
import com.bootcamp.myproject.application.model.Customer;
import com.bootcamp.myproject.application.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
@Tag(name = "customer", description = "Registro de clientes")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Registrar clientes")
    public Mono<Customer> saveCustomer(@Valid @RequestBody Customer customer) {
        return customerService.saveCustomer(customer);
    }

    @GetMapping
    @Operation(summary = "Consultar todos los clientes")
    public Flux<Customer> getAll() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{numDocument}")
    @Operation(summary = "Consultar cliente por numero de documento")
    public Mono<ResponseEntity<Customer>> getByNumDocument(@Parameter(description = "numero de documento", required = true) @PathVariable String numDocument) {
        return customerService.getCustomerByNumDocument(numDocument).map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar clientes")
    public Mono<ResponseEntity<Customer>> update(@PathVariable String id, @RequestBody Customer updated) {
        return customerService.updateCustomer(id, updated).map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Consultar productos por categoria")
    public Mono<ResponseEntity<ApiResponse<Void>>> delete(@PathVariable String id) {
        return customerService.deleteCustomer(id).then(Mono.just(ResponseEntity.ok(ApiResponse.noContent("Cliente eliminado correctamente"))));
    }

}
