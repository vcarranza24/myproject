package com.bootcamp.myproject.application.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private String timestamp;
    private int status;
    private String message;
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
