# Proposal

## Why

GitHub Issue [#12](https://github.com/user-bcp-student41/workshop-auth-backend/issues/12) addresses security and consistency gaps in the `POST /auth/login` token contract: the documented legacy token embeds the secret, its lifetime is reported as a fixed value, and it lacks the required client and timestamp claims. A verifiable token contract, safe secret configuration, and redacted logs make login tokens safer to issue and easier for clients to interpret.

## What Changes

### In Scope

- **BREAKING**: Require a nonblank `client` value in `POST /auth/login`; requests without it receive `400 Bad Request`. This changes the Day 1 contract, which currently accepts `client` as optional, per the clarification for Issue #12.
- Require successful login tokens to be signed and to carry `sub`, `client`, `iat`, and `exp`; `sub` identifies the authenticated user and `client` reflects the validated request value.
- Require `expiresIn` to agree with the token's expiration relative to its issuance time, within a documented one-second tolerance.
- Keep the laboratory secret explicitly limited to sandbox use; outside the sandbox, token issuance must use an explicitly configured `AUTH_TOKEN_SECRET`.
- Prohibit logging tokens, secrets, or other sensitive values.
- Preserve the existing login response envelope, user fields, and documented credential-error behavior. Decide HMAC versus a JWT library separately before implementation; this proposal does not select either.

### Out of Scope

- Changing credential lookup, user roles, the `user` response fields, the `debug` response field, or the documented 401/500 response contracts.
- Changing `GET /q/health`, CORS behavior, or unrelated runtime error handling.
- Selecting HMAC or a JWT library, token serialization details, or implementation structure; that technical decision is deferred to a later design.

### Expected Evidence

- Tests covering required claims, expiry, successful login, invalid credentials, missing/blank `client`, and expired-token behavior.
- Documentation identifying `AUTH_TOKEN_SECRET` and limiting any built-in default to the sandbox.
- Evidence that logs redact tokens, secrets, passwords, and other sensitive authentication values.

## Capabilities

### New Capabilities

- `auth-login-token`: observable requirements for login token claims, expiry, client validation, secret configuration, and sensitive-log redaction.

### Modified Capabilities

- None. The repository has no existing OpenSpec capabilities; the endpoint contract is documented in `docs/ENDPOINTS.md`.

## Impact

- API: `POST /auth/login` request validation and token semantics change; existing response fields and unrelated status/error behavior remain as documented in `docs/ENDPOINTS.md`.
- Configuration and operations: `AUTH_TOKEN_SECRET` must be documented as sandbox-only when using the lab default, and must be explicitly configured outside that environment; logs must not disclose sensitive values.
- Implementation technology: HMAC versus a JWT library remains undecided and is outside this proposal's design scope.