package com.bcp.workshop.auth;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class TokenServiceTest {
    private static final String TEST_SECRET =
            "TEST_ONLY_SECRET_FOR_QUARKUS_TESTS_32_BYTES_MINIMUM";

    @Inject
    TokenService tokenService;

    @Test
    void rejectsExpiredToken() throws Exception {
        String header = encode("""{"alg":"HS256","typ":"JWT"}""");
        String payload = encode("""
                {"sub":"student","client":"angular-workshop",
                 "iat":1,"exp":2}
                """);
        String unsigned = header + "." + payload;
        String token = unsigned + "." + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(sign(unsigned));

        assertThrows(InvalidTokenException.class, () -> tokenService.verify(token));
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(
                TEST_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return mac.doFinal(value.getBytes(StandardCharsets.US_ASCII));
    }
}