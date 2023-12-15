package com.example.demo.controller;

import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.exception.AppException;
import com.example.demo.service.JwtService;
import com.nimbusds.jose.JOSEException;

@RestController
@RequestMapping("jwt")
public class JwtController {

    private final JwtService jwtService;

    public JwtController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping("jwks")
    public Map<String, Object> jwks() {
        try {
            return jwtService.getPublicJwks();
        } catch (JOSEException e) {
            throw new AppException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("generate")
    public Map<String, Object> generateJWT(
            @RequestParam(name = "alg", required = false, defaultValue = "RS256") String algorithmName) {
        String token;
        try {
            token = jwtService.generateJwt(algorithmName);
        } catch (AppException
                | JOSEException e) {
            throw new AppException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("token", token);
        return res;
    }

    @GetMapping("verify")
    public Map<String, Object> verifyJWT(@RequestParam(name = "token") String token) {
        Map<String, Object> verificationResult;
        try {
            verificationResult = jwtService.verifyJwt(token);
        } catch (AppException
                | ParseException
                | JOSEException
                | CertificateExpiredException
                | CertificateNotYetValidException e) {
            throw new AppException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return verificationResult;
    }

}
