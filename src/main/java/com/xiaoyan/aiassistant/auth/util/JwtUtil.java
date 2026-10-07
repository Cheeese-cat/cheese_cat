// src/main/java/com/xiaoyan/aiassistant/auth/util/JwtUtil.java
package com.xiaoyan.aiassistant.auth.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:xiaoyan-ai-secret-key-2024-secure-jwt-signing-key}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    private byte[] getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            return padded;
        }
        return keyBytes;
    }

    public String generateToken(String username) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + expiration);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(username)
                .claim("username", username)
                .issueTime(now)
                .expirationTime(expiryDate)
                .build();

            JWSObject jwsObject = new JWSObject(
                new JWSHeader(JWSAlgorithm.HS256),
                new Payload(claimsSet.toJSONObject())
            );

            MACSigner signer = new MACSigner(getSigningKey());
            jwsObject.sign(signer);

            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("生成 JWT token 失败", e);
        }
    }

    public String extractUsername(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claimsSet = JWTClaimsSet.parse(signedJWT.getPayload().toJSONObject());
            return claimsSet.getSubject();
        } catch (ParseException e) {
            throw new RuntimeException("解析 JWT token 失败", e);
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claimsSet = JWTClaimsSet.parse(signedJWT.getPayload().toJSONObject());
            Date expirationTime = claimsSet.getExpirationTime();
            return expirationTime != null && expirationTime.before(new Date());
        } catch (ParseException e) {
            return true;
        }
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            MACVerifier verifier = new MACVerifier(getSigningKey());
            boolean verified = signedJWT.verify(verifier);
            if (!verified) {
                return false;
            }
            return !isTokenExpired(token);
        } catch (ParseException | JOSEException e) {
            return false;
        }
    }

    public boolean validateToken(String token, String username) {
        try {
            String extractedUsername = extractUsername(token);
            return extractedUsername.equals(username) && validateToken(token);
        } catch (Exception e) {
            return false;
        }
    }
}
