package com.example.demo.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppException extends RuntimeException {

    private HttpStatus status;

    public AppException(HttpStatus status) {
        super(status.getReasonPhrase());
        setStatus(status);
    }

    public AppException(String message, HttpStatus status) {
        super(message);
        setStatus(status);
    }

    public AppException(String message, HttpStatus status, Throwable e) {
        super(message, e);
        setStatus(status);
    }

}
