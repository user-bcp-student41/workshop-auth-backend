# Design

## Context

See `proposal.md` for motivation and scope, and `specs/auth-login-token/spec.md` for the required behavior. Today, `POST /auth/login` validates username/password, accepts `client` without using it, and returns `token`, fixed `expiresIn: 3600`, `user`, and `debug`. The token is Base64-encoded concatenated text containing username, role, expiry, and a hard-coded secret. `application.properties` has Quarkus `prod` profile configuration but no token-secret setting; `pom.xml` has no JWT dependency. The current resource test covers only a successful login.

## Goals / Non-Goals

**Goals:**
- Replace the custom token representation with a standard signed token that exposes the claims specified by the delta spec.
- Make the reported lifetime derive from the token timestamps while retaining the current one-hour lifetime.
- Keep the login response envelope and unrelated error contracts stable, with the explicitly approved `client` validation change.
- Make secret use profile-aware and prevent sensitive authentication values from reaching logs.
- Deliver the behavior in small, characterized increments.

**Non-Goals:**
- Add refresh, revocation, logout, or a token-verification endpoint.
- Change credential storage, role assignment, user response fields, `debug`, health checks, CORS, or the documented 401/500 response bodies.
- Add client registration/allowlisting, key rotation, multiple issuers/audiences, or a migration layer that accepts both token formats.
- Decide implementation details beyond the decisions required to meet the contract.

## Protected Contracts

- Preserve `POST /auth/login`, JSON request/response media types, and the successful response keys `token`, `expiresIn`, `user`, and `debug`.
- Preserve `user.username`, HTML-escaped `user.displayName`, and `user.role`.
- Preserve the documented 400 responses for null requests and missing/blank username or password, the 401 invalid-credentials response, and the 500 response shape.
- Preserve the one-hour token lifetime represented by `expiresIn: 3600`; the value must now agree with `exp - iat` within the spec's one-second tolerance.
- Intentionally change the Day 1 request contract: `client` is now required and a missing or blank value receives 400. A nonblank value is accepted without an allowlist and is represented unchanged in the `client` claim.
- The token's encoding and contents intentionally change; consumers must use the signed-token contract rather than decode the legacy Base64 concatenation.

## Components Affected

- `AuthResource`: request validation, token issuance boundary, response lifetime, and protection against sensitive logging.
- `LoginRequest`: existing `client` input becomes required by the login contract.
- `LegacyTokenUtil`: its custom Base64 token format and embedded secret are replaced by the selected signed-token format.
- `src/main/resources/application.properties` and deployment configuration: sandbox versus non-sandbox secret resolution.
- `pom.xml`: a Quarkus-compatible JWT/JWS builder dependency.
- `AuthResourceTest` and focused token tests: baseline characterization and new claims, expiry, validation, and redaction coverage.
- `docs/ENDPOINTS.md`: document the required `client`, changed token semantics, lifetime tolerance, and secret configuration.

## Decisions

### Use a standard JWT/JWS builder with HMAC-SHA-256

Use Quarkus-compatible SmallRye JWT build support to produce a compact signed JWT with HS256. JWT and HMAC are not competing choices here: JWT provides the standard claim-bearing format, while HMAC-SHA-256 provides the symmetric signature. Do not hand-roll token serialization or signing.

The existing Base64 concatenation is rejected because it is not a standard signed token, exposes the embedded secret to anyone who decodes it, and requires custom claim parsing. An asymmetric JWT algorithm is not selected: this service currently only issues tokens, no verifier or public-key distribution contract is documented, and adding those concerns would exceed this change.

### Derive claims and response lifetime from one issuance instant

Set `sub` from the authenticated username, `client` from the validated request value, and `iat` to the issuance time in Unix seconds. Set `exp` to `iat + 3600`; return `expiresIn` as `exp - iat`. Use UTC epoch seconds and the one-second tolerance defined by the spec for comparisons. This avoids independently calculated token and response expiries while preserving the documented one-hour duration.

### Separate sandbox and non-sandbox secret configuration

Resolve the signing key from `AUTH_TOKEN_SECRET` in non-sandbox deployments; treat Quarkus `prod` as non-sandbox and fail startup if the setting is absent. Keep any laboratory default scoped only to `dev`/`test`, and document it as unsuitable for deployment. Never include the configured value in API responses or application logs. Secret rotation and key lifecycle policy remain outside this change.

### Characterize the existing API before changing token behavior

Before modifying login behavior, add tests that capture the Day 1 response keys and user fields, HTML escaping, the exact documented 400 username/password cases, invalid-credential 401 shape, and the documented 500 shape where the test harness can induce that failure. The baseline success test should assert only that the legacy token is present; it must not codify its Base64 contents or embedded secret as a desired contract. Add separate tests for the new required-client behavior and signed claims before replacing the token format.

### Implement and roll out incrementally

1. Land characterization coverage and confirm it passes against the current behavior.
2. Add focused tests for absent/blank client, claim contents, signature verification, expiry coherence/boundary, and log redaction.
3. Add the selected JWT support and profile-aware secret configuration, then replace token issuance while keeping response fields stable.
4. Update endpoint documentation and verify the complete login/error contract and deployment configuration before rollout.

This is a sequencing strategy, not a task breakdown; detailed implementation tasks are intentionally deferred.

## Risks / Trade-offs

- **Client and token format are breaking changes** -> Coordinate API consumers to send `client` and stop decoding the legacy token before rollout; no dual-format compatibility is included.
- **Missing or weak production secret prevents secure issuance** -> Require explicit non-sandbox configuration and keep the laboratory default out of `prod`; operational secret strength/rotation policy needs separate ownership.
- **HMAC verification requires sharing the secret with any verifier** -> No verifier or distribution mechanism is introduced here; confirm consumers can securely share the configured key before rollout.
- **Clock skew can affect expiry comparisons** -> Define timestamps in UTC epoch seconds and keep comparisons within the documented one-second tolerance; tests should control or bound time.
- **Logs can expose values through error or request logging** -> Review authentication-path logging and exercise redaction checks for both successful and failed login paths.
- **The documented 500 body exposes exception details** -> Preserve it for this change as required by the approved scope; address that security concern separately rather than silently changing the response contract.

## Rollback

If rollout must be reverted, redeploy the prior release and require affected users/clients to authenticate again; the prior release issues the legacy token format and cannot continue the new signed-token contract. Keep the externally managed `AUTH_TOKEN_SECRET` available during rollback, but do not copy it into the old hard-coded secret path. Because this change does not provide dual-format compatibility, verify consumer readiness before rollout and treat rollback as a coordinated API rollback rather than a transparent token migration.