package com.bcp.workshop.auth;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class AuthResourceTest {
    private static final long EXPIRES_IN_TOLERANCE_SECONDS = 5;

    @Inject
    TokenService tokenService;

    @Test
    void loginEmitsSignedTokenWithRequiredClaims() {
        Response response = given()
                .contentType("application/json")
                .body("""
                        {"username":"student","password":"Workshop123!",
                         "client":"angular-workshop"}
                        """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String token = response.jsonPath().getString("token");
        long expiresIn = response.jsonPath().getLong("expiresIn");
        TokenService.TokenClaims claims = tokenService.verify(token);

        assertEquals("student", claims.subject());
        assertEquals("angular-workshop", claims.client());
        assertTrue(claims.issuedAt() > 0);
        assertTrue(claims.expiresAt() > claims.issuedAt());

        long secondsRemaining = claims.expiresAt() - System.currentTimeMillis() / 1000;
        assertTrue(Math.abs(expiresIn - secondsRemaining)
                        <= EXPIRES_IN_TOLERANCE_SECONDS,
                "expiresIn debe corresponder a exp - now dentro de la tolerancia");
    }

    @Test
    void rejectsInvalidCredentialsWithGenericError() {
        given()
                .contentType("application/json")
                .body("""
                        {"username":"student","password":"wrong",
                         "client":"angular-workshop"}
                        """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401)
                .body("message", org.hamcrest.Matchers.is("Invalid credentials"));
    }

    @Test
    void rejectsInvalidClientAtTheRequestBoundary() {
        given()
                .contentType("application/json")
                .body("""
                        {"username":"student","password":"Workshop123!",
                         "client":"client with spaces"}
                        """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(400)
                .body("message", org.hamcrest.Matchers.is("Invalid login request"));
    }
}