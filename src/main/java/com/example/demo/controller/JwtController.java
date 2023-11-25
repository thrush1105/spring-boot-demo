package com.example.demo.controller;

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
import com.nimbusds.jwt.SignedJWT;

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
            return jwtService.getJwks();
        } catch (JOSEException e) {
            throw new AppException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("generate")
    public Map<String, Object> generateJWT() {
        String token;
        try {
            token = jwtService.generateJwt();
        } catch (JOSEException e) {
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
        } catch (ParseException | JOSEException e) {
            throw new AppException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        boolean verified = (boolean) verificationResult.get("verified");
        SignedJWT signedJWT = (SignedJWT) verificationResult.get("signedJWT");

        if (!verified) {
            throw new AppException("Invalid signature", HttpStatus.BAD_REQUEST);
        }

        return signedJWT.getPayload().toJSONObject();
    }

}
