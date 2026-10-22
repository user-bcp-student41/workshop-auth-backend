package com.bcp.workshop.auth;

import io.vertx.core.json.JsonObject;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.Base64;
import java.util.regex.Pattern;

@ApplicationScoped
public class SignedTokenService {
    private static final String DEFAULT_TTL_SECONDS = "3600";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String JWT_ALGORITHM = "HS256";
    private static final String INVALID_TOKEN_MESSAGE = "Invalid token";
    private static final int MINIMUM_SECRET_BYTES = 32;
    private static final long ISSUED_AT_CLOCK_SKEW_SECONDS = 1;
    private static final int MAX_TOKEN_LENGTH = 8192;
    private static final Pattern CLIENT_PATTERN =
            Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,63}");

    @ConfigProperty(name = "auth.token.secret")
    String secret;

    @ConfigProperty(
            name = "auth.token.expiration-seconds",
            defaultValue = DEFAULT_TTL_SECONDS)
    long tokenTtlSeconds;

    private Clock clock;
    private byte[] signingKey;

    public SignedTokenService() {
        this.clock = Clock.systemUTC();
    }

    SignedTokenService(String secret, long tokenTtlSeconds, Clock clock) {
        this.secret = secret;
        this.tokenTtlSeconds = tokenTtlSeconds;
        this.clock = clock;
        initialize();
    }

    @PostConstruct
    void initialize() {
        if (secret == null
                || secret.getBytes(StandardCharsets.UTF_8).length < MINIMUM_SECRET_BYTES) {
            throw new IllegalStateException(
                    "AUTH_TOKEN_SECRET must contain at least 32 bytes");
        }
        if (tokenTtlSeconds <= 0) {
            throw new IllegalStateException(
                    "auth.token.expiration-seconds must be positive");
        }
        if (clock == null) {
            clock = Clock.systemUTC();
        }
        signingKey = secret.getBytes(StandardCharsets.UTF_8);
    }

    public IssuedToken issue(String subject, String client) {
        validateSubject(subject);
        validateClient(client);

        long issuedAt = clock.instant().getEpochSecond();
        long expiresAt;
        try {
            expiresAt = Math.addExact(issuedAt, tokenTtlSeconds);
        } catch (ArithmeticException exception) {
            throw new IllegalStateException("Token expiration is out of range");
        }

        JsonObject header = new JsonObject()
                .put("alg", JWT_ALGORITHM)
                .put("typ", "JWT");
        JsonObject payload = new JsonObject()
                .put("sub", subject)
                .put("client", client)
                .put("iat", issuedAt)
                .put("exp", expiresAt);

        String unsignedToken = encode(header.encode()) + "." + encode(payload.encode());
        String token = unsignedToken + "." + encode(sign(unsignedToken));
        long expiresIn = Math.max(0, expiresAt - clock.instant().getEpochSecond());

        return new IssuedToken(token, expiresIn);
    }

    public VerifiedToken verify(String token) {
        if (token == null || token.isBlank() || token.length() > MAX_TOKEN_LENGTH) {
            throw invalidToken();
        }

        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3) {
                throw invalidToken();
            }

            JsonObject header = decodeJson(parts[0]);
            if (!JWT_ALGORITHM.equals(header.getValue("alg"))) {
                throw invalidToken();
            }

            byte[] suppliedSignature = Base64.getUrlDecoder().decode(parts[2]);
            byte[] expectedSignature = sign(parts[0] + "." + parts[1]);
            if (!MessageDigest.isEqual(expectedSignature, suppliedSignature)) {
                throw invalidToken();
            }

            JsonObject claims = decodeJson(parts[1]);
            String subject = readStringClaim(claims, "sub");
            String client = readStringClaim(claims, "client");
            long issuedAt = readLongClaim(claims, "iat");
            long expiresAt = readLongClaim(claims, "exp");
            long now = clock.instant().getEpochSecond();

            validateSubject(subject);
            validateClient(client);
            if (expiresAt <= issuedAt
                    || issuedAt > now + ISSUED_AT_CLOCK_SKEW_SECONDS
                    || expiresAt <= now) {
                throw invalidToken();
            }

            return new VerifiedToken(subject, client, issuedAt, expiresAt);
        } catch (TokenValidationException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw invalidToken();
        }
    }

    public static boolean isValidClient(String client) {
        return client != null && CLIENT_PATTERN.matcher(client).matches();
    }

    private static void validateSubject(String subject) {
        if (subject == null || subject.isBlank() || subject.length() > 128) {
            throw new IllegalArgumentException("Invalid token subject");
        }
    }

    private static void validateClient(String client) {
        if (!isValidClient(client)) {
            throw new IllegalArgumentException("Invalid login request");
        }
    }

    private byte[] sign(String content) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(signingKey, HMAC_ALGORITHM));
            return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Token signing failed");
        }
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private static JsonObject decodeJson(String encoded) {
        byte[] decoded = Base64.getUrlDecoder().decode(encoded);
        return new JsonObject(new String(decoded, StandardCharsets.UTF_8));
    }

    private static String readStringClaim(JsonObject claims, String name) {
        Object value = claims.getValue(name);
        if (!(value instanceof String stringValue) || stringValue.isBlank()) {
            throw invalidToken();
        }
        return stringValue;
    }

    private static long readLongClaim(JsonObject claims, String name) {
        Object value = claims.getValue(name);
        if (!(value instanceof Integer || value instanceof Long)) {
            throw invalidToken();
        }
        return ((Number) value).longValue();
    }

    private static TokenValidationException invalidToken() {
        return new TokenValidationException(INVALID_TOKEN_MESSAGE);
    }

    public record IssuedToken(String value, long expiresIn) {}

    public record VerifiedToken(
            String subject,
            String client,
            long issuedAt,
            long expiresAt) {}

    public static final class TokenValidationException extends RuntimeException {
        public TokenValidationException(String message) {
            super(message);
        }
    }
}