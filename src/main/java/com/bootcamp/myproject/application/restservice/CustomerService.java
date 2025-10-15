package com.bootcamp.myproject.application.restservice;

import com.bootcamp.myproject.application.restrepository.CustomerRepository;
import com.bootcamp.myproject.application.restrepository.TypeCustomerRepository;
import com.bootcamp.myproject.application.restrepository.TypeDocumentRepository;
import com.bootcamp.myproject.application.model.Customer;
import com.bootcamp.myproject.application.util.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.Set;


@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final TypeCustomerRepository typeCustomerRepository;
    private final TypeDocumentRepository typeDocumentRepository;

    private static final Set<String> PERSONAL_DOCS = Set.of("DNI", "Pasaporte", "Carnet extranjeria");

    /**
     * CREATE
     * Save a customer with business validation:
     * - PERSONAL customers can only have DNI, Pasaporte, Carnet Extranjeria.
     * - BUSINESS customers can only have RUC.
     */
    public Mono<Customer> saveCustomer(Customer customer) {
        return customerRepository.findByNumDocument(customer.getNumDocument()).flatMap(existing -> Mono.<Customer>error(new BusinessException("Ya existe un cliente registrado con el número de documento: " + customer.getNumDocument()))).switchIfEmpty(Mono.defer(() -> Mono.zip(typeCustomerRepository.findById(customer.getIdTypeCustomer()), typeDocumentRepository.findById(customer.getIdTypeDocument()), Tuples::of).flatMap(tuple -> {
            String typeCustomer = tuple.getT1().getNameCustomer();  // personal o empresarial
            String typeDocument = tuple.getT2().getNameDocument();  // DNI, RUC, etc.
            String lastName = customer.getLastName();

            return validateCustomer(typeCustomer, typeDocument, lastName).flatMap(valid -> {
                if (!valid) {
                    return Mono.error(new BusinessException(typeDocument + " no válido para el cliente " + typeCustomer));
                }
                return customerRepository.save(customer).doOnSuccess(c -> log.info("cliente guardado con id={}", c.getId())).doOnError(e -> log.error("Error al guardar al cliente: {}", e.getMessage()));
            });
        }).switchIfEmpty(Mono.<Customer>error(new BusinessException("Tipo de cliente o documento no encontrado")))));

    }

    /**
     * READ (Listar todos los clientes)
     */

    public Flux<Customer> getAllCustomers() {
        return customerRepository.findAll().doOnError(e -> log.error(" Error al listar clientes: {}", e.getMessage()));
    }

    /**
     * search for DNI
     */

    public Mono<Customer> getCustomerByNumDocument(String numDocument) {
        return customerRepository.findByNumDocument(numDocument).switchIfEmpty(Mono.error(new BusinessException("Cliente no encontrado con num_documento: " + numDocument))).doOnSuccess(c -> log.info(" Cliente encontrado: {}", c.getNumDocument())).doOnError(e -> log.error(" Error al buscar cliente por ID: {}", e.getMessage()));
    }

    /**
     * update customer
     */

    public Mono<Customer> updateCustomer(String id, Customer updatedCustomer) {
        return customerRepository.findById(id).switchIfEmpty(Mono.error(new BusinessException("Cliente no encontrado con ID: " + id))).flatMap(existing -> {
            existing.setName(updatedCustomer.getName());
            existing.setLastName(updatedCustomer.getLastName());
            existing.setNumDocument(updatedCustomer.getNumDocument());
            existing.setEmail(updatedCustomer.getEmail());
            existing.setAddress(updatedCustomer.getAddress());
            existing.setPhone(updatedCustomer.getPhone());
            return customerRepository.save(existing).doOnSuccess(c -> log.info(" Cliente actualizado: {}", c.getId()));
        });
    }

    /**
     * delete customer
     */

    public Mono<Void> deleteCustomer(String id) {
        return customerRepository.findById(id).switchIfEmpty(Mono.error(new BusinessException("Cliente no encontrado con ID: " + id))).flatMap(customer -> customerRepository.delete(customer).doOnSuccess(v -> log.info("🗑️ Cliente eliminado físicamente: {}", id)));
    }

    /**
     * VALIDACIONES
     */


    private Mono<Boolean> validateCustomer(String typeCustomer, String typeDocument, String lastName) {

        if (typeCustomer == null || typeDocument == null) {
            return Mono.error(new BusinessException("Tipo de cliente y tipo de documento son requeridos"));
        }


        // Simulando mapeo (ejemplo si traes los nombres de BD)
        if ("personal".equalsIgnoreCase(typeCustomer)) {

            boolean validDoc = PERSONAL_DOCS.stream().anyMatch(d -> d.equalsIgnoreCase(typeDocument));

            if (!validDoc) {
                return Mono.just(false);
            }

            // lastName requerido para documentos personales
            if (lastName == null || lastName.trim().isEmpty()) {
                return Mono.error(new BusinessException("El campo lastName es obligatorio para clientes personales"));
            }

            return Mono.just(true);

        } else if ("empresarial".equalsIgnoreCase(typeCustomer)) {
            return Mono.just("RUC".equalsIgnoreCase(typeDocument));
        }

        return Mono.just(false);
    }

}
