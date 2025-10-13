package com.bootcamp.myproject.application.util;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Estructura estándar de respuesta de la API")
public class ApiResponse<T> {
    private String timestamp;
    @Schema(description = "Código de estado HTTP", example = "200")
    private int status;
    @Schema(description = "Mensaje descriptivo de la respuesta", example = "Operación exitosa")
    private String message;
    @Schema(description = "Datos devueltos por la API (puede ser nulo)")
    private T data;

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(LocalDateTime.now().toString(), 200, message, data);
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(LocalDateTime.now().toString(), 201, message, data);
    }

    public static <T> ApiResponse<T> noContent(String message) {
        return new ApiResponse<>(LocalDateTime.now().toString(), 204, message, null);
    }
}
