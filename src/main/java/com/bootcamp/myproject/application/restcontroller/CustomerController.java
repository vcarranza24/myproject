package com.bootcamp.myproject.application.restcontroller;

import com.bootcamp.myproject.application.restservice.CustomerService;
import com.bootcamp.myproject.application.model.Customer;
import com.bootcamp.myproject.application.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public Mono<Customer> saveCustomer(@Valid @RequestBody Customer customer) {
        return customerService.saveCustomer(customer);
    }

    @GetMapping
    public Flux<Customer> getAll() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{numDocument}")
    public Mono<ResponseEntity<Customer>> getByNumDocument(@PathVariable String numDocument) {
        return customerService.getCustomerByNumDocument(numDocument)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Customer>> update(@PathVariable String id, @RequestBody Customer updated) {
        return customerService.updateCustomer(id, updated)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> delete(@PathVariable String id) {
        return customerService.deleteCustomer(id)
                .then(Mono.just(
                        ResponseEntity.ok(ApiResponse.noContent("Cliente eliminado correctamente"))
                ));
    }

}
