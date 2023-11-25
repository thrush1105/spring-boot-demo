package com.example.demo.service;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Service
public class JwtService {

    @Autowired
    private Environment environment;

    private final JWK privateJwk;

    private final JWK publicJwk;

    public JwtService()
            throws JOSEException {
        privateJwk = new RSAKeyGenerator(2048)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID(UUID.randomUUID().toString())
                .generate();
        publicJwk = privateJwk.toPublicJWK();
    }

    public Map<String, Object> getJwks()
            throws JOSEException {
        JWKSet jwkSet = new JWKSet(publicJwk);
        jwkSet.containsJWK(publicJwk);
        return jwkSet.toJSONObject();
    }

    public String generateJwt()
            throws JOSEException {
        Date now = new Date();

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(environment.getProperty("jwt.issuer"))
                .audience(environment.getProperty("jwt.audience"))
                .issueTime(now)
                .expirationTime(new Date(now.getTime()
                        + Long.valueOf(environment.getProperty("jwt.expiration", "300")) * 1000))
                .jwtID(UUID.randomUUID().toString())
                .build();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(publicJwk.getKeyID())
                .build();

        JWSSigner signer = new RSASSASigner(privateJwk.toRSAKey());

        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    public Map<String, Object> verifyJwt(String token)
            throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        RSASSAVerifier verifier = new RSASSAVerifier(publicJwk.toRSAKey());
        boolean verified = signedJWT.verify(verifier);

        Map<String, Object> verificationResult = new HashMap<>();
        verificationResult.put("verified", verified);
        verificationResult.put("signedJWT", signedJWT);
        return verificationResult;
    }

}
