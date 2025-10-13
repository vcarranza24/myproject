package com.bootcamp.myproject.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RefreshScope
@RestController
public class ConfigTestController {

    @Value("${custom.message:Mensaje por defecto}")
    private String message;

    @GetMapping("/mensaje")
    public String mensaje() {
        return message;
    }
}
