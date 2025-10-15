package com.bootcamp.myproject.service;

import com.bootcamp.myproject.application.model.Accounts;
import com.bootcamp.myproject.application.model.Customer;
import com.bootcamp.myproject.application.model.TypeAccount;
import com.bootcamp.myproject.application.model.TypeCustomer;
import com.bootcamp.myproject.application.restrepository.CustomerRepository;
import com.bootcamp.myproject.application.restrepository.TypeAccountRepository;
import com.bootcamp.myproject.application.restrepository.TypeCustomerRepository;
import com.bootcamp.myproject.application.restservice.AccountsService;
import com.bootcamp.myproject.application.restservice.ParameterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static com.bootcamp.myproject.application.model.CustomerProfile.REGULAR;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private TypeCustomerRepository typeCustomerRepository;
    @Mock
    private TypeAccountRepository typeAccountRepository;
    @Mock
    private ParameterService parameterService;

    @InjectMocks
    private AccountsService accountService; // Tu clase que contiene createAccount()

    @Test
    void createPersonalAccount_withValidMinimumBalance_shouldSucceed() {
        // Datos simulados
        Accounts account = new Accounts();
        account.setIdCustomer("CUST123");
        account.setIdTypeAccount("TYPE_ACC_PERSONAL");
        account.setBalance(BigDecimal.ZERO);

        Customer customer = new Customer();
        customer.setId("CUST123");
        customer.setIdTypeCustomer("TYPE_CUST_PERSONAL");
        customer.setProfile(REGULAR);
        customer.setHasCreditCard(false);

        TypeCustomer typeCustomer = new TypeCustomer();
        typeCustomer.setId("TYPE_CUST_PERSONAL");
        typeCustomer.setNameCustomer("personal");

        TypeAccount typeAccount = new TypeAccount();
        typeAccount.setId("TYPE_ACC_PERSONAL");
        typeAccount.setNameAccount("Ahorro");

        // 🔧 Mock de dependencias
        when(customerRepository.findById("CUST123")).thenReturn(Mono.just(customer));
        when(typeCustomerRepository.findById("TYPE_CUST_PERSONAL")).thenReturn(Mono.just(typeCustomer));
        when(typeAccountRepository.findById("TYPE_ACC_PERSONAL")).thenReturn(Mono.just(typeAccount));

        // El parámetro configurado en Mongo
        when(parameterService.getDouble("MIN_OPENING_AMOUNT_PERSONAL")).thenReturn(Mono.just(0.0));

        // Ejecución
        Mono<Accounts> result = accountService.createAccount(account);

        // Verificación
        StepVerifier.create(result)
                .expectNextMatches(acc -> acc.getBalance() == BigDecimal.valueOf(100))
                .verifyComplete();
    }
}
