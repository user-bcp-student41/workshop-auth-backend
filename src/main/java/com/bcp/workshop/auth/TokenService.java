package com.bcp.workshop.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@ApplicationScoped
public class TokenService {
    private static final String ALGORITHM = "HS256";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final long TOKEN_LIFETIME_SECONDS = 3600;
    private static final long CLOCK_SKEW_TOLERANCE_SECONDS = 5;
    private static final int MINIMUM_SECRET_BYTES = 32;
    private static final int MAXIMUM_TOKEN_LENGTH = 8192;

    private final byte[] secret;
    private final ObjectMapper objectMapper;
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    private final Base64.Decoder decoder = Base64.getUrlDecoder();

    @Inject
    public TokenService(
            @ConfigProperty(name = "auth.token.secret") String secret,
            ObjectMapper objectMapper) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.objectMapper = objectMapper;

        if (this.secret.length < MINIMUM_SECRET_BYTES) {
            throw new IllegalStateException("Token signing configuration is invalid");
        }
    }

    public IssuedToken issue(String subject, String client) {
        if (subject == null || subject.isBlank()
                || !LoginRequestValidator.isValidClient(client)) {
            throw new IllegalArgumentException("Invalid token claims");
        }

        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + TOKEN_LIFETIME_SECONDS;

        Map<String, Object> header = Map.of("alg", ALGORITHM, "typ", "JWT");
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", subject);
        claims.put("client", client);
        claims.put("iat", issuedAt);
        claims.put("exp", expiresAt);

        String unsignedToken = encode(header) + "." + encode(claims);
        String token = unsignedToken + "." + encoder.encodeToString(sign(unsignedToken));

        return new IssuedToken(token, expiresAt - issuedAt);
    }

    public TokenClaims verify(String token) {
        if (token == null || token.isBlank() || token.length() > MAXIMUM_TOKEN_LENGTH) {
            throw new InvalidTokenException();
        }

        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3) {
                throw new InvalidTokenException();
            }

            byte[] suppliedSignature = decoder.decode(parts[2]);
            byte[] expectedSignature = sign(parts[0] + "." + parts[1]);
            if (!MessageDigest.isEqual(expectedSignature, suppliedSignature)) {
                throw new InvalidTokenException();
            }

            JsonNode header = objectMapper.readTree(decoder.decode(parts[0]));
            if (!ALGORITHM.equals(header.path("alg").asText())
                    || !"JWT".equals(header.path("typ").asText())) {
                throw new InvalidTokenException();
            }

            JsonNode claims = objectMapper.readTree(decoder.decode(parts[1]));
            String subject = claims.path("sub").asText(null);
            String client = claims.path("client").asText(null);
            long issuedAt = requiredLong(claims, "iat");
            long expiresAt = requiredLong(claims, "exp");
            long now = Instant.now().getEpochSecond();

            if (subject == null || subject.isBlank()
                    || !LoginRequestValidator.isValidClient(client)
                    || expiresAt <= issuedAt
                    || expiresAt <= now
                    || issuedAt > now + CLOCK_SKEW_TOLERANCE_SECONDS) {
                throw new InvalidTokenException();
            }

            return new TokenClaims(subject, client, issuedAt, expiresAt);
        } catch (InvalidTokenException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidTokenException();
        }
    }

    private String encode(Object value) {
        try {
            return encoder.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception exception) {
            throw new IllegalStateException("Token could not be created");
        }
    }

    private long requiredLong(JsonNode claims, String name) {
        JsonNode value = claims.get(name);
        if (value == null || !value.isIntegralNumber()) {
            throw new InvalidTokenException();
        }
        return value.longValue();
    }

    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return mac.doFinal(value.getBytes(StandardCharsets.US_ASCII));
        } catch (Exception exception) {
            throw new IllegalStateException("Token signing is unavailable");
        }
    }

    public record IssuedToken(String token, long expiresIn) {}

    public record TokenClaims(String subject, String client, long issuedAt, long expiresAt) {}
}