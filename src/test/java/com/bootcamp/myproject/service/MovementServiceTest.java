package com.bootcamp.myproject.service;

import com.bootcamp.myproject.application.model.Accounts;
import com.bootcamp.myproject.application.model.TypeMovement;
import com.bootcamp.myproject.application.restrepository.AccountsRepository;
import com.bootcamp.myproject.application.restrepository.TypeMovementRepository;
import com.bootcamp.myproject.application.restservice.AccountsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;

import static org.mockito.Mockito.verify;
import static reactor.core.publisher.Mono.when;

@ExtendWith(MockitoExtension.class)
public class MovementServiceTest {

    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private TypeMovementRepository typeMovementRepository;

    @InjectMocks
    private AccountsService accountsService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void depositAccount_fallbackOnTimeout() {
        // Simular cuenta existente
        when(accountsRepository.existsByNumAccount("001-0004888888")).thenReturn(Mono.just(true));

        Accounts acc = new Accounts();
        acc.setNumAccount("001-0004888888");
        acc.setBalance(BigDecimal.valueOf(1000.0));
        acc.setState(1);

        when(accountsRepository.findByNumAccount("001-0004888888")).thenReturn(Mono.just(acc));

        // Simular que el repo demora más de lo permitido
        when(typeMovementRepository.findByNameMovementIgnoreCase("deposito"))
                .thenReturn(Mono.delay(Duration.ofSeconds(5))
                        .thenReturn(new TypeMovement("68e7c4470ace661a5024e695", "deposito")));
        // Ejecutar
        Mono<Accounts> result = accountsService.depositAccount("001-0004888888", BigDecimal.valueOf(500.0));

        StepVerifier.create(result)
                .expectNextMatches(a -> a.getState() == 0 && a.getBalance() ==BigDecimal.valueOf(0.0) &&
                        "001-0004888888".equals(acc.getNumAccount()))
                .verifyComplete();

        verify(accountsRepository).existsByNumAccount("001-0004888888");
        verify(typeMovementRepository).findByNameMovementIgnoreCase("deposito");
    }
}
