package com.emergency.routing.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(
        { IllegalArgumentException.class, IllegalStateException.class }
    )
    public ResponseEntity<Map<String, String>> handleBadRequest(
        RuntimeException ex
    ) {
        return ResponseEntity.badRequest().body(
            Map.of("error", ex.getMessage())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
        MethodArgumentNotValidException ex
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            Map.of("error", "Validation failed")
        );
    }
}
