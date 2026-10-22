# Token de autenticación — Issue #12

- Decisión técnica: mantener HMAC con JWT `HS256`; no se añade una dependencia
  JWT en este cambio.
- `sub` contiene el username autenticado. `client` contiene el identificador
  validado de la solicitud.
- Claims obligatorios: `sub`, `client`, `iat` y `exp`.
- Duración: 3600 segundos. `expiresIn` se calcula como `exp - iat`; se admite
  una diferencia máxima de 5 segundos al comparar con `exp - now`, por el
  tiempo transcurrido entre emisión y comprobación.
- `client` acepta identificadores de 1 a 64 caracteres alfanuméricos, punto,
  guion y guion bajo; no es una lista de autorización de clientes.
- El secreto se proporciona mediante `AUTH_TOKEN_SECRET`, con al menos 32 bytes.
  No se registra ni se define un valor productivo por defecto.
- Los tokens Base64 heredados no se consideran válidos. Antes del despliegue
  hay que confirmar compatibilidad con consumidores y planificar la expiración
  de los tokens existentes.
- El secreto de `src/test/resources/application.properties` es exclusivo de
  pruebas aisladas.
- No se añade esquema, se conservan los datos existentes y no se crean
  migraciones: este cambio solo afecta a la emisión/verificación de tokens.