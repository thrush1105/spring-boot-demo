package com.example.demo.controller;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.exception.AppException;
import com.example.demo.service.JwkService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@RestController
@RequestMapping("jwt")
public class JwtController {

    @Autowired
    private Environment environment;

    private final JwkService jwkService;

    public JwtController(JwkService jwkService) {
        this.jwkService = jwkService;
    }

    @GetMapping("")
    public Map<String, Object> generateJWT() throws JOSEException {
        Date now = new Date();

        RSAKey rsaKey = jwkService.getRsaKey();
        RSAKey rsaPublicKey = jwkService.getRsaPublicKey();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(rsaKey.getKeyID())
                .build();

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(environment.getProperty("jwt.issuer"))
                .audience(environment.getProperty("jwt.audience"))
                .issueTime(now)
                .expirationTime(new Date(now.getTime()
                        + Long.valueOf(environment.getProperty("jwt.expiration", "300")) * 1000))
                .jwtID(UUID.randomUUID().toString())
                .claim("foo", "bar")
                .build();

        JWSSigner signer = new RSASSASigner(rsaKey);

        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        signedJWT.sign(signer);

        Map<String, Object> decoded = new HashMap<>();
        decoded.put("header", header.toJSONObject());
        decoded.put("payload", claimsSet.toJSONObject());

        Map<String, Object> response = new HashMap<>();
        response.put("encoded", signedJWT.serialize());
        response.put("decoded", decoded);
        response.put("jwk", rsaPublicKey.toJSONObject());

        return response;
    }

    @GetMapping("verify")
    public Map<String, Object> verifyJWT(@RequestParam(name = "token") String token) {
        RSAKey rsaPublicKey = jwkService.getRsaPublicKey();

        SignedJWT signedJWT;
        try {
            signedJWT = SignedJWT.parse(token);
        } catch (ParseException e) {
            throw new AppException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        boolean verified;
        try {
            RSASSAVerifier verifier = new RSASSAVerifier(rsaPublicKey);
            verified = signedJWT.verify(verifier);
        } catch (JOSEException e) {
            throw new AppException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> decoded = new HashMap<>();
        decoded.put("header", signedJWT.getHeader().toJSONObject());
        decoded.put("payload", signedJWT.getPayload().toJSONObject());

        Map<String, Object> response = new HashMap<>();
        response.put("encoded", token);
        response.put("decoded", decoded);
        response.put("verified", verified);
        response.put("jwk", rsaPublicKey.toJSONObject());

        return response;
    }

}
