package com.bootcamp.myproject.application.client;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AccountClient {

    public Mono<Boolean> validateAccount(String idAccount) {
        // Simulamos una llamada lenta o inestable
        if ("ACC-ERROR".equals(idAccount)) {
            return Mono.error(new RuntimeException("Error remoto"));
        }

        if ("ACC-SLOW".equals(idAccount)) {
            // Simula un servicio que demora más de 2 segundos (causará timeout)
            return Mono.delay(java.time.Duration.ofSeconds(3))
                    .thenReturn(true);
        }

        // Caso normal: responde rápido
        return Mono.just(true);
    }
}
