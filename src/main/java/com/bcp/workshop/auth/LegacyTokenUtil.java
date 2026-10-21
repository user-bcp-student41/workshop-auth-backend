package com.bcp.workshop.auth;

import io.quarkus.runtime.Startup;
import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;

@ApplicationScoped
@Startup
public class LegacyTokenUtil {
    private static final int TOKEN_LIFETIME_SECONDS = 3600;

    @Inject
    @ConfigProperty(name = "auth.token.secret")
    String secret;

    public LegacyTokenUtil() {}

    LegacyTokenUtil(String secret) {
        this.secret = secret;
    }

    public IssuedToken create(String username, String client) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + TOKEN_LIFETIME_SECONDS;
        String token = Jwt.claims()
                .subject(username)
                .claim("client", client)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .jws()
                .algorithm(SignatureAlgorithm.HS256)
                .type("JWT")
                .sign(new SecretKeySpec(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
        return new IssuedToken(token, (int) (expiresAt - issuedAt));
    }

    public record IssuedToken(String value, int expiresIn) {}
}
