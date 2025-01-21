package com.dsapkl.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CustomClientAlertException extends RuntimeException {
    public CustomClientAlertException(String message) {
        super(message);
    }
}
