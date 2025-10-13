package com.bootcamp.myproject.application.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleBusiness(BusinessException ex) {
        return Mono.just(
                ResponseEntity.badRequest().body(
                        Map.of(
                                "timestamp", LocalDateTime.now().toString(),
                                "status", 400,
                                "error", "Business rule violation",
                                "message", ex.getMessage()
                        )
                )
        );
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidation(WebExchangeBindException ex) {
        var errors = ex.getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.toList());

        return Mono.just(
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        Map.of(
                                "timestamp", LocalDateTime.now().toString(),
                                "status", 400,
                                "error", "Validation error",
                                "messages", errors
                        )
                )
        );
    }

}
