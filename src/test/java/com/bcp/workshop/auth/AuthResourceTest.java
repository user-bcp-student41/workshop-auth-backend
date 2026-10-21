package com.bcp.workshop.auth;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class AuthResourceTest {
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
        resource.database = new LegacyDatabase() {
            @Override
            public UserRecord findByCredentials(String username, String password) {
                return new UserRecord(1, username, "<b>Workshop</b>", "STUDENT");
            }
        };

        LoginRequest request = new LoginRequest();
        request.username = "student";
        request.password = "Workshop123!";

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
}
