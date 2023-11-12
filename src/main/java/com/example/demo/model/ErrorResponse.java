package com.example.demo.model;

import com.example.demo.exception.AppException;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {

    private int statusCode;

    private String message;

    private StackTraceElement[] stackTrace;

    public ErrorResponse(AppException e) {
        setStatusCode(e.getStatus().value());
        setMessage(e.getMessage());
        setStackTrace(e.getStackTrace());
    }

}
