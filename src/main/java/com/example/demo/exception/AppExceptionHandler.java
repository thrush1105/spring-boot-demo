package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.example.demo.model.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class AppExceptionHandler {

    @ExceptionHandler({ AppException.class })
    public ResponseEntity<Object> handleAppException(AppException e) {
        HttpStatus status = e.getStatus();
        ErrorResponse response = new ErrorResponse(e);
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleException(Exception e) {
        HttpStatus status;
        if (e instanceof MissingServletRequestParameterException
                || e instanceof MethodArgumentTypeMismatchException
                || e instanceof MethodArgumentNotValidException) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            log.error(e.getMessage(), e);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        ErrorResponse response = new ErrorResponse(status.value(), e.getMessage(), e.getStackTrace());
        return ResponseEntity.status(status).body(response);
    }

}
