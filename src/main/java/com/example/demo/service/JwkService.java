package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;

import lombok.Getter;

@Service
public class JwkService {

    @Getter
    private final RSAKey rsaKey;

    @Getter
    private final RSAKey rsaPublicKey;

    public JwkService()
            throws JOSEException {
        rsaKey = new RSAKeyGenerator(2048)
                .keyID("1")
                .generate();
        rsaPublicKey = rsaKey.toPublicJWK();
    }

}
