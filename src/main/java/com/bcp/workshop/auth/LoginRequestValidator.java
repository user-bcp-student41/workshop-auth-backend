package com.bcp.workshop.auth;

import java.util.regex.Pattern;

public final class LoginRequestValidator {
    private static final int MAX_USERNAME_LENGTH = 128;
    private static final int MAX_PASSWORD_LENGTH = 1024;
    private static final Pattern CLIENT_PATTERN =
            Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,63}");

    private LoginRequestValidator() {}

    public static boolean isValid(LoginRequest request) {
        return request != null
                && hasText(request.username)
                && request.username.length() <= MAX_USERNAME_LENGTH
                && hasText(request.password)
                && request.password.length() <= MAX_PASSWORD_LENGTH
                && isValidClient(request.client);
    }

    public static boolean isValidClient(String client) {
        return client != null && CLIENT_PATTERN.matcher(client).matches();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}