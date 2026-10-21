package com.bcp.workshop.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.ws.rs.core.Response;
import io.quarkus.test.junit.QuarkusTestProfile;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
@TestProfile(AuthResourceTest.TokenTestProfile.class)
class AuthResourceTest {
    private static final String TEST_SIGNING_SECRET = "test-only-auth-token-secret-with-more-than-32-bytes";
    private static final ObjectMapper JSON = new ObjectMapper();

    public static class TokenTestProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("auth.token.secret", TEST_SIGNING_SECRET);
        }
    }

    @Test
    void logsInWithWorkshopUser() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":\"Workshop123!\",\"client\":\"angular-workshop\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(200)
                .body("user.username", is("student"))
                .body("user.displayName", is("Workshop Student"))
                .body("user.role", is("STUDENT"))
                .body("token", notNullValue())
                .body("expiresIn", is(3600))
                .body("debug", is("authenticated-with-legacy-flow"));
    }

    @Test
    void escapesHtmlInDisplayName() {
        AuthResource resource = new AuthResource();
        resource.tokenUtil = new LegacyTokenUtil(TEST_SIGNING_SECRET);
        resource.database = new LegacyDatabase() {
            @Override
            public UserRecord findByCredentials(String username, String password) {
                return new UserRecord(1, username, "<b>Workshop</b>", "STUDENT");
            }
        };

        LoginRequest request = new LoginRequest();
        request.username = "student";
        request.password = "Workshop123!";
        request.client = "angular-workshop";

        Response response = resource.login(request);
        assertEquals(200, response.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getEntity();
        @SuppressWarnings("unchecked")
        Map<String, Object> userBody = (Map<String, Object>) responseBody.get("user");
        assertEquals("&lt;b&gt;Workshop&lt;/b&gt;", userBody.get("displayName"));
    }

    @Test
    void returns400WhenRequestIsNull() {
        AuthResource resource = new AuthResource();

        Response response = resource.login(null);
        assertEquals(400, response.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getEntity();
        assertEquals("Request is null", responseBody.get("message"));
    }

    @Test
    void returns400WhenUsernameIsMissing() {
        given()
                .contentType("application/json")
                .body("{\"password\":\"Workshop123!\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(400)
                .body("message", is("Username is required"));
    }

    @Test
    void returns400WhenUsernameIsBlank() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"   \",\"password\":\"Workshop123!\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(400)
                .body("message", is("Username is required"));
    }

    @Test
    void returns400WhenPasswordIsMissing() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"student\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(400)
                .body("message", is("Password is required"));
    }

    @Test
    void returns400WhenPasswordIsBlank() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":\" \"}")
                .when().post("/auth/login")
                .then()
                .statusCode(400)
                .body("message", is("Password is required"));
    }

    @Test
    void returns401WhenCredentialsAreInvalid() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":\"wrong\",\"client\":\"angular-workshop\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(401)
                .body("message", is("Invalid user student or password"));
    }

    @Test
    void returns500WithDocumentedErrorFieldsWhenLoginFailsUnexpectedly() {
        AuthResource resource = new AuthResource();
        resource.database = new LegacyDatabase() {
            @Override
            public UserRecord findByCredentials(String username, String password) {
                throw new RuntimeException("boom");
            }
        };

        LoginRequest request = new LoginRequest();
        request.username = "student";
        request.password = "Workshop123!";
        request.client = "angular-workshop";

        Response response = resource.login(request);
        assertEquals(500, response.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getEntity();
        assertEquals("boom", responseBody.get("message"));
        assertEquals(RuntimeException.class.getName(), responseBody.get("exception"));
    }

    @Test
    void rejectsLoginWhenClientIsMissing() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":\"Workshop123!\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(400)
                .body("token", nullValue());
    }

    @Test
    void rejectsLoginWhenClientIsBlank() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":\"Workshop123!\",\"client\":\"   \"}")
                .when().post("/auth/login")
                .then()
                .statusCode(400)
                .body("token", nullValue());
    }

    @Test
    void issuesSignedTokenWithRequiredClaimsAndCoherentExpiry() throws Exception {
        io.restassured.response.Response loginResponse = given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":\"Workshop123!\",\"client\":\"angular-workshop\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(200)
                .extract().response();

        String token = loginResponse.jsonPath().getString("token");
        String[] tokenParts = token.split("\\.", -1);
        assertEquals(3, tokenParts.length);

        JsonNode header = JSON.readTree(Base64.getUrlDecoder().decode(tokenParts[0]));
        JsonNode claims = JSON.readTree(Base64.getUrlDecoder().decode(tokenParts[1]));
        assertEquals("HS256", header.path("alg").asText());
        assertEquals("student", claims.path("sub").asText());
        assertEquals("angular-workshop", claims.path("client").asText());

        long issuedAt = claims.path("iat").asLong();
        long expiresAt = claims.path("exp").asLong();
        int expiresIn = loginResponse.jsonPath().getInt("expiresIn");
        assertTrue(expiresAt > issuedAt);
        assertEquals(issuedAt + 3600, expiresAt);
        assertEquals(3600, expiresIn);
        assertTrue(Math.abs((expiresAt - issuedAt) - expiresIn) <= 1);
        assertFalse(isExpiredAt(expiresAt - 1, expiresAt));
        assertTrue(isExpiredAt(expiresAt, expiresAt));
        assertTrue(isExpiredAt(expiresAt + 1, expiresAt));
        assertFalse(token.contains(TEST_SIGNING_SECRET));

        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(new SecretKeySpec(TEST_SIGNING_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] expectedSignature = hmac.doFinal((tokenParts[0] + "." + tokenParts[1]).getBytes(StandardCharsets.US_ASCII));
        assertArrayEquals(expectedSignature, Base64.getUrlDecoder().decode(tokenParts[2]));
    }

    @Test
    void doesNotLogSensitiveValuesWhenLoginSucceeds() {
        CapturedLogs<io.restassured.response.Response> captured = captureLogs(() -> given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":\"Workshop123!\",\"client\":\"angular-workshop\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(200)
                .extract().response());

        String token = captured.result().jsonPath().getString("token");
        assertFalse(token == null || token.isBlank());
        assertFalse(captured.output().contains(token));
        assertFalse(captured.output().contains(TEST_SIGNING_SECRET));
        assertFalse(captured.output().contains("Workshop123!"));
    }

    @Test
    void doesNotLogSensitiveValuesWhenLoginFails() {
        String password = "wrong-password-for-log-check";
        CapturedLogs<io.restassured.response.Response> captured = captureLogs(() -> given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":\"" + password + "\",\"client\":\"angular-workshop\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(401)
                .extract().response());

        assertFalse(captured.output().contains(password));
        assertFalse(captured.output().contains(TEST_SIGNING_SECRET));
    }

    private static boolean isExpiredAt(long currentEpochSecond, long expirationEpochSecond) {
        return currentEpochSecond >= expirationEpochSecond;
    }

    private static <T> CapturedLogs<T> captureLogs(Supplier<T> action) {
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Handler handler = new StreamHandler(output, new SimpleFormatter()) {
            @Override
            public synchronized void publish(java.util.logging.LogRecord record) {
                super.publish(record);
                flush();
            }
        };
        handler.setLevel(Level.ALL);
        rootLogger.addHandler(handler);
        try {
            T result = action.get();
            handler.flush();
            return new CapturedLogs<>(result, output.toString(StandardCharsets.UTF_8));
        } finally {
            rootLogger.removeHandler(handler);
            handler.close();
        }
    }

    private record CapturedLogs<T>(T result, String output) {}
}
