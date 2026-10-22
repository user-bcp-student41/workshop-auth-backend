package com.bcp.workshop.auth;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SignedTokenServiceTest {
    private static final String TEST_SECRET =
            "unit-test-secret-with-more-than-32-bytes";
    private static final long TEST_EPOCH_SECONDS = 1_800_000_000L;

    @Test
    void issuesTokenWithRequiredClaimsAndCoherentExpiration() {
        Clock clock = Clock.fixed(
                Instant.ofEpochSecond(TEST_EPOCH_SECONDS),
                ZoneOffset.UTC);
        SignedTokenService service = new SignedTokenService(TEST_SECRET, 3600, clock);

        SignedTokenService.IssuedToken issued =
                service.issue("student", "angular-workshop");
        SignedTokenService.VerifiedToken verified = service.verify(issued.value());

        assertEquals("student", verified.subject());
        assertEquals("angular-workshop", verified.client());
        assertEquals(TEST_EPOCH_SECONDS, verified.issuedAt());
        assertEquals(TEST_EPOCH_SECONDS + 3600, verified.expiresAt());
        assertEquals(verified.expiresAt() - TEST_EPOCH_SECONDS, issued.expiresIn());
    }

    @Test
    void rejectsExpiredToken() {
        Clock issuerClock = Clock.fixed(
                Instant.ofEpochSecond(TEST_EPOCH_SECONDS),
                ZoneOffset.UTC);
        SignedTokenService issuer =
                new SignedTokenService(TEST_SECRET, 60, issuerClock);
        String token = issuer.issue("student", "angular-workshop").value();

        Clock expiredClock = Clock.fixed(
                Instant.ofEpochSecond(TEST_EPOCH_SECONDS + 60),
                ZoneOffset.UTC);
        SignedTokenService verifier =
                new SignedTokenService(TEST_SECRET, 60, expiredClock);

        assertThrows(
                SignedTokenService.TokenValidationException.class,
                () -> verifier.verify(token));
    }

    @Test
    void rejectsTokenWithInvalidSignature() {
        Clock clock = Clock.fixed(
                Instant.ofEpochSecond(TEST_EPOCH_SECONDS),
                ZoneOffset.UTC);
        SignedTokenService service = new SignedTokenService(TEST_SECRET, 3600, clock);
        String token = service.issue("student", "angular-workshop").value();
        String tamperedToken = token.substring(0, token.length() - 1) + "A";

        assertThrows(
                SignedTokenService.TokenValidationException.class,
                () -> service.verify(tamperedToken));
    }

    @Test
    void rejectsMalformedClient() {
        Clock clock = Clock.fixed(
                Instant.ofEpochSecond(TEST_EPOCH_SECONDS),
                ZoneOffset.UTC);
        SignedTokenService service = new SignedTokenService(TEST_SECRET, 3600, clock);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.issue("student", "angular workshop"));
    }
}