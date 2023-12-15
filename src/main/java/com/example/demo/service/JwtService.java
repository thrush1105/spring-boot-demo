package com.example.demo.service;

import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.example.demo.exception.AppException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jose.util.X509CertUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Service
public class JwtService {

    private final List<String> supportedAlgorithms;

    private final JWKSet publicjJwkSet;

    private final Map<String, JWSSigner> signers = new HashMap<>();

    private final Map<String, JWSVerifier> verifiers = new HashMap<>();

    public JwtService()
            throws JOSEException {
        supportedAlgorithms = Arrays.asList(
                JWSAlgorithm.HS256.getName(),
                JWSAlgorithm.RS256.getName(),
                JWSAlgorithm.RS512.getName(),
                JWSAlgorithm.ES256.getName());

        List<JWK> jwks = new ArrayList<>();

        // HS256
        byte[] sharedSecret = new byte[32];
        new SecureRandom().nextBytes(sharedSecret);
        signers.put(JWSAlgorithm.HS256.getName(), new MACSigner(sharedSecret));
        verifiers.put(JWSAlgorithm.HS256.getName(), new MACVerifier(sharedSecret));

        // RS256
        RSAKey rsaKey = new RSAKeyGenerator(2048)
                .algorithm(JWSAlgorithm.RS256)
                .generate();
        RSAKey rsaPublicKey = rsaKey.toPublicJWK();
        jwks.add(rsaPublicKey);
        signers.put(JWSAlgorithm.RS256.getName(), new RSASSASigner(rsaKey));
        verifiers.put(JWSAlgorithm.RS256.getName(), new RSASSAVerifier(rsaPublicKey));

        // RS512
        RSAKey rsa4096Key = new RSAKeyGenerator(4096)
                .algorithm(JWSAlgorithm.RS512)
                .generate();
        RSAKey rsa4096PublicKey = rsa4096Key.toPublicJWK();
        jwks.add(rsa4096PublicKey);
        signers.put(JWSAlgorithm.RS512.getName(), new RSASSASigner(rsa4096Key));
        verifiers.put(JWSAlgorithm.RS512.getName(), new RSASSAVerifier(rsa4096PublicKey));

        // ES256
        ECKey ecKey = new ECKeyGenerator(Curve.P_256)
                .algorithm(JWSAlgorithm.ES256)
                .generate();
        ECKey ecPublicKey = ecKey.toPublicJWK();
        jwks.add(ecPublicKey);
        signers.put(JWSAlgorithm.ES256.getName(), new ECDSASigner(ecKey));
        verifiers.put(JWSAlgorithm.ES256.getName(), new ECDSAVerifier(ecPublicKey));

        publicjJwkSet = new JWKSet(jwks);
    }

    public Map<String, Object> getPublicJwks()
            throws JOSEException {
        return publicjJwkSet.toJSONObject();
    }

    private void validateAlgorithm(String algorithmName) {
        if (!supportedAlgorithms.contains(algorithmName)) {
            throw new AppException(
                    String.format("Supported algorithms are ", String.join(", ", supportedAlgorithms)));
        }
    }

    public String generateJwt(String algorithmName)
            throws JOSEException {
        validateAlgorithm(algorithmName);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issueTime(new Date())
                .build();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.parse(algorithmName))
                .type(JOSEObjectType.JWT)
                .build();

        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        signedJWT.sign(signers.get(algorithmName));

        return signedJWT.serialize();
    }

    public Map<String, Object> verifyJwt(String token)
            throws ParseException, JOSEException, CertificateExpiredException, CertificateNotYetValidException {
        SignedJWT signedJWT = SignedJWT.parse(token);

        String algorithmName = signedJWT.getHeader().getAlgorithm().getName();

        validateAlgorithm(algorithmName);

        boolean verified;
        if (Objects.nonNull(signedJWT.getHeader().getX509CertChain())) {
            verified = verifyJwtIncludingX509CertChain(signedJWT);
        } else {
            verified = signedJWT.verify(verifiers.get(algorithmName));
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
        PublicKey publicKey = cert.getPublicKey();
        JWSVerifier verifier;
        if (publicKey instanceof RSAPublicKey) {
            verifier = new RSASSAVerifier((RSAPublicKey) publicKey);
        } else if (publicKey instanceof ECPublicKey) {
            verifier = new ECDSAVerifier((ECPublicKey) publicKey);
        } else {
            throw new AppException("Error in JWT verification");
        }
        return signedJWT.verify(verifier);
    }

    private void verifyCert(X509Certificate cert)
            throws CertificateExpiredException, CertificateNotYetValidException {
        cert.checkValidity();
    }

}
