package com.example.demo.service;

import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jose.util.X509CertUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Service
public class JwtService {

    @Autowired
    private Environment environment;

    private final JWSAlgorithm algorithm;

    private final JWK privateJwk;

    private final JWK publicJwk;

    private final JWSSigner signer;

    private final JWSVerifier verifier;

    public JwtService()
            throws JOSEException {
        algorithm = JWSAlgorithm.RS256;
        privateJwk = new RSAKeyGenerator(2048)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(algorithm)
                .keyID(UUID.randomUUID().toString())
                .generate();
        publicJwk = privateJwk.toPublicJWK();
        signer = new RSASSASigner(privateJwk.toRSAKey());
        verifier = new RSASSAVerifier(publicJwk.toRSAKey());
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

        JWSHeader header = new JWSHeader.Builder(algorithm)
                .keyID(publicJwk.getKeyID())
                .build();

        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    public Map<String, Object> verifyJwt(String token)
            throws ParseException, JOSEException, CertificateExpiredException, CertificateNotYetValidException {
        SignedJWT signedJWT = SignedJWT.parse(token);

        boolean verified;
        if (Objects.nonNull(signedJWT.getHeader().getX509CertChain())) {
            verified = verifyJwtIncludingX509CertChain(signedJWT);
        } else {
            verified = signedJWT.verify(verifier);
        }

        Map<String, Object> decoded = new HashMap<>();
        decoded.put("header", signedJWT.getHeader().toJSONObject());
        decoded.put("payload", signedJWT.getPayload().toJSONObject());

        Map<String, Object> verificationResult = new HashMap<>();
        verificationResult.put("verified", verified);
        verificationResult.put("encoded", signedJWT.serialize());
        verificationResult.put("decoded", decoded);
        return verificationResult;
    }

    private boolean verifyJwtIncludingX509CertChain(SignedJWT signedJWT)
            throws ParseException, JOSEException, CertificateExpiredException, CertificateNotYetValidException {
        List<Base64> certChain = signedJWT.getHeader().getX509CertChain();
        X509Certificate cert = X509CertUtils.parse(certChain.get(0).decode());
        verifyCert(cert);
        RSAPublicKey publicKey = (RSAPublicKey) cert.getPublicKey();
        JWSVerifier verifier = new RSASSAVerifier(publicKey);
        return signedJWT.verify(verifier);
    }

    private void verifyCert(X509Certificate cert)
            throws CertificateExpiredException, CertificateNotYetValidException {
        cert.checkValidity();
    }

}
