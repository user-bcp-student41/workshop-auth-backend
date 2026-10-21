# Spec Delta

## Purpose

This capability defines the observable contract for signed tokens issued by `POST /auth/login`, including the required client identity, token claims and expiry, secret configuration, and protection of sensitive values.

## ADDED Requirements

### Requirement: Login requires a nonblank client
The system SHALL require a non-null, nonblank `client` string for `POST /auth/login` and reject a missing or blank value with `400 Bad Request`. This change intentionally replaces the Day 1 contract's optional `client` field; any nonblank string is valid, and this change introduces no client allowlist.

#### Scenario: Client is absent
- **WHEN** a request has valid nonblank username and password but omits `client`
- **THEN** the login responds with `400 Bad Request` and does not issue a token

#### Scenario: Client is blank
- **WHEN** a request has valid credentials and `client` is empty or whitespace-only
- **THEN** the login responds with `400 Bad Request` and does not issue a token

### Requirement: Successful login issues a signed token with required claims
For valid credentials and a nonblank `client`, the system SHALL return a signed, integrity-protected token containing `sub`, `client`, `iat`, and `exp`. `sub` SHALL identify the authenticated user's username, `client` SHALL equal the validated request value, and `iat` and `exp` SHALL represent Unix epoch timestamps in seconds with `exp` later than `iat`. The token SHALL NOT disclose the signing secret.

#### Scenario: Valid login returns the required claims
- **WHEN** a user submits valid credentials and a nonblank client value
- **THEN** the response is `200 OK`, the token's signature is verifiable, and its claims contain the user's username as `sub`, the submitted client as `client`, and ordered `iat` and `exp` timestamps

### Requirement: Expiration and expiresIn are coherent
The system SHALL report `expiresIn` as an integer number of seconds consistent with `exp - iat`, using a documented maximum tolerance of one second. A token SHALL be considered expired when the current time is at or after `exp`.

#### Scenario: Response lifetime matches token expiration
- **WHEN** a successful login response is inspected
- **THEN** its `expiresIn` matches the token's `exp - iat` within one second

#### Scenario: Token reaches its expiration boundary
- **WHEN** the current time is equal to or later than a token's `exp`
- **THEN** the token is expired and is not treated as valid

### Requirement: Existing login response and credential-error contract is preserved
Except for the intentional token semantics and the newly required `client`, the system SHALL preserve the Day 1 `POST /auth/login` response fields, user data, and documented credential-error status and body behavior.

#### Scenario: Successful response retains documented fields
- **WHEN** login succeeds with valid credentials and client
- **THEN** the `200 OK` JSON response contains `token`, `expiresIn`, `user`, and `debug`; `user` retains `username`, HTML-escaped `displayName`, and `role`

#### Scenario: Missing username or password retains its response
- **WHEN** the request is null, or username or password is null, empty, or blank
- **THEN** login responds with the corresponding documented `400 Bad Request` message from the Day 1 contract

#### Scenario: Invalid credentials retain their response
- **WHEN** username or password does not match an existing user and `client` is valid
- **THEN** login responds with the documented `401 Unauthorized` error shape and does not issue a token

#### Scenario: Unexpected login failure retains its documented status and shape
- **WHEN** an unhandled error occurs during login
- **THEN** login responds with the documented `500 Internal Server Error` fields; changing that response contract is outside this capability

### Requirement: The laboratory signing secret is not used outside the sandbox
The system SHALL document any built-in laboratory secret as sandbox-only and SHALL require an explicitly configured `AUTH_TOKEN_SECRET` for token signing outside the sandbox. The secret SHALL NOT be returned to clients.

#### Scenario: Token signing outside the sandbox
- **WHEN** login is served outside the sandbox
- **THEN** issued tokens use an explicitly configured `AUTH_TOKEN_SECRET`, not the laboratory default

#### Scenario: Secret is not exposed in the response
- **WHEN** login succeeds or fails
- **THEN** no response contains the secret value

### Requirement: Sensitive authentication values are not logged
The system SHALL NOT write tokens, signing secrets, passwords, or other sensitive authentication values to application logs during login or token issuance.

#### Scenario: Successful login logs are redacted
- **WHEN** a login succeeds and a token is issued
- **THEN** application logs contain neither the token nor the signing secret or password

#### Scenario: Failed login logs are redacted
- **WHEN** login fails because of invalid credentials or invalid client input
- **THEN** application logs contain no password, signing secret, or token value