package com.bcp.workshop.auth;

@Deprecated(forRemoval = true)
public final class LegacyTokenUtil {
    private LegacyTokenUtil() {}

    /**
     * Los tokens Base64 heredados no se emiten. Los llamadores deben usar
     * TokenService y proporcionar el cliente recibido por la solicitud.
     */
    @Deprecated(forRemoval = true)
    public static String create(String username, String role) {
        throw new UnsupportedOperationException("Legacy token issuance is disabled");
    }
}