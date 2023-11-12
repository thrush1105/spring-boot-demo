package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.exception.AppException;

@RestController
@RequestMapping("exception")
public class ExceptionController {

    @GetMapping("app")
    public void throwAppException(
            @RequestParam(name = "status_code", required = false, defaultValue = "400") Integer statusCode) {
        HttpStatus status = HttpStatus.valueOf(statusCode);
        throw new AppException(status);
    }

}
