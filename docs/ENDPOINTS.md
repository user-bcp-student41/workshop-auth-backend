# Contrato HTTP del backend de autenticación

Documento generado a partir del código y de [HU-01.md](../HU-01.md), porque el
repositorio no tenía un contrato escrito. Es el contrato que hay que preservar
al refactorizar. Cualquier cambio que no respete esta tabla requiere
coordinación con los consumidores.

## Consumidores conocidos

- **Frontend Angular** (`workshop-login-frontend`): hace `POST /auth/login` con
  `{username, password, client}` y guarda `token`, `user` y `expiresIn`. Usa
  `token` como `Authorization: Bearer` contra la API Python.
- **API Python** (`:8000`): consume el token. Su forma de validarlo **no está
  en el alcance de este repositorio**; cualquier cambio de formato de token debe
  coordinarse con ese componente antes de desplegar.

## POST /auth/login

- Ruta: `POST http://localhost:8080/auth/login`
- Request: `Content-Type: application/json`
- Body (`LoginRequest`): `username` (string), `password` (string), `client`
  (string, opcional). `client` se deserializa pero **no se usa** en el código
  actual (ver [AuthResource.java](src/main/java/com/bcp/workshop/auth/AuthResource.java)).

### Respuestas

| Caso | HTTP | Cuerpo |
|---|---|---|
| Éxito | 200 | `{"token": string, "expiresIn": 3600, "user": {"username": string, "displayName": string (HTML-escapado), "role": string}, "debug": "authenticated-with-legacy-flow"}` |
| Body nulo | 400 | `{"message": "Request is null"}` |
| `username` nulo o en blanco | 400 | `{"message": "Username is required"}` |
| `password` nulo o en blanco | 400 | `{"message": "Password is required"}` |
| Credenciales inválidas | 401 | `{"message": "Invalid user <username> or password"}` |
| Excepción no controlada | 500 | `{"message": <detalle o "Unknown error">, "exception": <nombre de clase>}` |

Referencias en código: [AuthResource.java:24-58](src/main/java/com/bcp/workshop/auth/AuthResource.java#L24-L58).

### Formato actual del token

`Base64( username + "|" + role + "|" + expiresEpochSeconds + "|" + SECRET )`,
con `SECRET` embebido y `expiresIn` fijado en 3600 s. Ver
[LegacyTokenUtil.java:8-15](src/main/java/com/bcp/workshop/auth/LegacyTokenUtil.java#L8-L15).
Este formato es el que hay que reemplazar (H-03); el contrato externo que debe
mantenerse es la presencia del campo `token` y de `expiresIn`.

### Observaciones (no son parte del contrato a mantener sin decisión)

- `debug` es un campo informativo; no se sabe si algún consumidor lo lee.
- Los errores de base de datos se tragan en
  [LegacyDatabase.java:39-42](src/main/java/com/bcp/workshop/auth/LegacyDatabase.java#L39-L42)
  y se devuelven como 401, por lo que un fallo de BD no produce 500.
- Las pruebas existentes solo cubren el caso 200
  ([AuthResourceTest.java:10-22](src/test/java/com/bcp/workshop/auth/AuthResourceTest.java#L10-L22)).

## Configuración relevante

- `AUTH_DB_URL` (opcional, por defecto `jdbc:sqlite:./data/workshop.db`).
- `AUTH_TOKEN_SECRET`: el código actual **no** lo lee; el secreto está fijo en
  código (ver HALLAZGOS H-04).
- Puerto `8080`, CORS abierto a cualquier origen (ver [application.properties](src/main/resources/application.properties)).
- Health: `GET /q/health`.
