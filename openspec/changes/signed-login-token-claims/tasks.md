# Tasks

## 1. Characterize the Day 1 Login Contract

- [x] 1.1 Expand login success tests to assert the documented response keys, user fields, HTML-escaped display name, `debug`, and one-hour `expiresIn`, without asserting the legacy token's encoded contents; verify with `mvn test -Dtest=AuthResourceTest`.
- [x] 1.2 Add characterization tests for null request, missing/blank username or password, and invalid credentials, asserting the documented status codes and response bodies; verify the focused authentication tests pass against the current behavior.

## 2. Require and Validate the Client

- [ ] 2.1 Add tests that omit `client` and provide an empty or whitespace-only value, asserting `400 Bad Request` and no token; verify these tests fail against the current implementation for the intended reason.
- [ ] 2.2 Require a nonblank `client` for login and preserve the submitted value for token issuance; verify the missing/blank cases return 400 and a nonblank value reaches the successful-login path.

## 3. Configure and Issue Signed Tokens

- [ ] 3.1 Add tests for the token contract: verifiable signature, `sub`, `client`, `iat`, `exp`, no exposed secret, and `expiresIn` coherence within one second; verify the tests fail against the legacy token behavior.
- [ ] 3.2 Add JWT/JWS signing support and issue tokens with the specified claims, deriving `exp` and `expiresIn` from the same issuance time while retaining the 3600-second lifetime; verify the claims, signature, and expiry-boundary tests pass.
- [ ] 3.3 Configure `AUTH_TOKEN_SECRET` for non-sandbox operation and constrain any laboratory default to sandbox/test profiles; verify non-sandbox startup fails without the explicit secret and succeeds with it, without exposing the value.

## 4. Prevent Sensitive Log Disclosure

- [ ] 4.1 Add successful- and failed-login log-capture checks and ensure tokens, secrets, passwords, and other sensitive authentication values are absent; verify the redaction checks pass.

## 5. Document and Verify the Integrated Contract

- [ ] 5.1 Update `docs/ENDPOINTS.md` with required `client`, signed-token claims, one-second expiry tolerance, the 3600-second lifetime, and sandbox/non-sandbox `AUTH_TOKEN_SECRET` requirements; verify its examples and status codes match the spec.
- [ ] 5.2 Run the full Maven test suite and `openspec validate signed-login-token-claims --type change --strict`; verify both complete successfully and the documented login response/error contracts remain intact.