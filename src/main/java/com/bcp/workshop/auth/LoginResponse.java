package com.bcp.workshop.auth;

public record LoginResponse(String token, long expiresIn, User user) {
    public record User(long id, String username, String displayName, String role) {
        public static User from(UserRecord record) {
            return new User(
                    record.id(),
                    record.username(),
                    record.displayName(),
                    record.role());
        }
    }
}