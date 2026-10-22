# HU-01 — Token firmado con expiración y claims mínimos

Trazabilidad: Issue #12. El identificador H-xx no estaba incluido en el
contexto recibido y queda pendiente; no se inventa.

## Decisión técnica

Se conserva HMAC-SHA-256 y se emite un JWT compacto con `alg=HS256`.
El payload contiene `sub`, `client`, `iat` y `exp`. La validación comprueba
firma, claims y expiración antes de aceptar el token.

`client` admite de 1 a 64 caracteres ASCII: letras, números, punto, guion
y guion bajo; debe comenzar con letra o número. Esta validación sintáctica
no define una allowlist de clientes.

`expiresIn` se calcula como `exp - now` al emitir el token. La diferencia
respecto a `exp - now` observada por el cliente puede ser de hasta un segundo
por el avance del reloj entre emisión y recepción. La prueba con reloj fijo
comprueba el valor exacto.

## Configuración y logs

Configurar `AUTH_TOKEN_SECRET` en el entorno de ejecución con al menos
32 bytes aleatorios. No se proporciona un secreto por defecto. El secreto
del perfil `%test` es exclusivo del laboratorio. No registrar el secreto,
el token, la contraseña ni el cuerpo sensible del login.

## Integración pendiente en el endpoint activo

Después de autenticar las credenciales y antes de construir la respuesta,
el endpoint debe validar `request.hasValidClient()`, emitir con: