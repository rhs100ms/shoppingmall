package com.dsapkl.backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomClientAlertException.class)
    public ResponseEntity<?> handleCustomClientAlertException(CustomClientAlertException ex) {
        // JSON 형태로 메시지를 반환
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }
}
