# Hallazgos del backend

## Alcance y criterio

Inspección estática limitada a este repositorio, sin modificar código ni consultar
los otros componentes del workspace. Esta es la única matriz de seguimiento;
los pasos posteriores de `2-refactor.md` deben actualizar aquí cada hallazgo.

Se cubren los siete casos de [RETO.md](RETO.md). Todos los patrones sembrados se
encontraron en el código, así que no hay filas **No encontrado**. Las severidades
priorizan el riesgo potencial en este laboratorio; no son puntuaciones CVSS ni
explotaciones demostradas.

Este archivo no existía en el repositorio y se generó a partir de RETO.md y del código.
La evidencia de dependencias se verificó con una fuente externa (ver H-06); el resto
es evidencia estática, no ejecución de exploits.

## Hallazgos

id | hallazgo | tipo | explicacion | evidencia (archivo:linea) | severidad | estado
---|---|---|---|---|---|---
H-01 | Concatenación de credenciales en SQL | Vulnerabilidad | `username` y `password` se insertan directamente en la sentencia. Un valor que altere las comillas cambia la consulta: puede saltarse la verificación de contraseña o leer datos ajenos a la tabla. | [LegacyDatabase.java:32](src/main/java/com/bcp/workshop/auth/LegacyDatabase.java#L32), [LegacyDatabase.java:35](src/main/java/com/bcp/workshop/auth/LegacyDatabase.java#L35) | alta | Pendiente
H-02 | Contraseñas en texto plano | Vulnerabilidad | La columna `password` guarda el valor sin hash ni salt, las credenciales de demo están en el código y la comparación se hace en SQL. Si la base se filtra o se copia, las contraseñas son utilizables de inmediato. | [LegacyDatabase.java:22](src/main/java/com/bcp/workshop/auth/LegacyDatabase.java#L22), [LegacyDatabase.java:23](src/main/java/com/bcp/workshop/auth/LegacyDatabase.java#L23), [LegacyDatabase.java:24](src/main/java/com/bcp/workshop/auth/LegacyDatabase.java#L24), [LegacyDatabase.java:32](src/main/java/com/bcp/workshop/auth/LegacyDatabase.java#L32) | alta | Pendiente
H-03 | Token Base64 con secreto embebido y sin firma | Vulnerabilidad | Base64 es una codificación reversible, no una firma. Quien decodifique el token obtiene el secreto y puede fabricar tokens para cualquier usuario y rol. El token no lleva claims `sub`, `client` ni `iat`, y la expiración es solo un número dentro del texto, sin verificación. | [LegacyTokenUtil.java:13](src/main/java/com/bcp/workshop/auth/LegacyTokenUtil.java#L13), [LegacyTokenUtil.java:14](src/main/java/com/bcp/workshop/auth/LegacyTokenUtil.java#L14), [LegacyTokenUtil.java:15](src/main/java/com/bcp/workshop/auth/LegacyTokenUtil.java#L15), [AuthResource.java:39](src/main/java/com/bcp/workshop/auth/AuthResource.java#L39) | alta | Pendiente
H-04 | Secreto de firma y CORS hardcodeados | Vulnerabilidad | El secreto `WORKSHOP_SECRET_2026` está en código fuente: cualquiera con acceso al repositorio puede falsificar tokens. `AUTH_TOKEN_SECRET`, que HU-01 menciona, no se lee en ningún lado. CORS acepta cualquier origen (`/.*/`), así que cualquier sitio puede llamar al login desde el navegador de un usuario. | [LegacyTokenUtil.java:8](src/main/java/com/bcp/workshop/auth/LegacyTokenUtil.java#L8), [application.properties:5](src/main/resources/application.properties#L5), [application.properties:7](src/main/resources/application.properties#L7) | alta | Pendiente
H-05 | Respuestas de error exponen detalles internos | Vulnerabilidad | El 500 devuelve `getMessage()` y el nombre de la clase de la excepción. El 401 refleja el `username` enviado, sin escapar en el JSON. La base de datos imprime stacktraces en la salida estándar. Esto da información útil para el reconocimiento de un atacante y no tiene trazabilidad controlada. | [AuthResource.java:37](src/main/java/com/bcp/workshop/auth/AuthResource.java#L37), [AuthResource.java:56](src/main/java/com/bcp/workshop/auth/AuthResource.java#L56), [AuthResource.java:57](src/main/java/com/bcp/workshop/auth/AuthResource.java#L57), [LegacyDatabase.java:27](src/main/java/com/bcp/workshop/auth/LegacyDatabase.java#L27), [LegacyDatabase.java:40](src/main/java/com/bcp/workshop/auth/LegacyDatabase.java#L40) | media | Pendiente
H-06 | Dependencia vulnerable: Commons Text 1.9 (CVE-2022-42889) | Vulnerabilidad | Text4Shell afecta a Commons Text 1.5 a 1.9 y se corrigió en 1.10.0 (fuente: avisos de Apache). El código solo usa `StringEscapeUtils.escapeHtml4`, no los interpoladores peligrosos, así que la explotabilidad aquí es limitada, pero la dependencia debe actualizarse o retirarse. Quarkus 3.27.4 no se verificó contra advisories en esta inspección. | [pom.xml:49](pom.xml#L49), [pom.xml:50](pom.xml#L50), [pom.xml:13](pom.xml#L13), [AuthResource.java:10](src/main/java/com/bcp/workshop/auth/AuthResource.java#L10), [AuthResource.java:45](src/main/java/com/bcp/workshop/auth/AuthResource.java#L45) | media | Pendiente
H-07 | Resource con validación, negocio, persistencia y respuesta | Refactor | `AuthResource.login` valida la entrada, llama a la base, genera el token, arma la respuesta y maneja errores en un solo método, con cuatro niveles de `if` anidados y el número mágico `3600`. Cambiar cualquier capa obliga a tocar el endpoint, y es difícil probar cada regla por separado. | [AuthResource.java:24](src/main/java/com/bcp/workshop/auth/AuthResource.java#L24), [AuthResource.java:25](src/main/java/com/bcp/workshop/auth/AuthResource.java#L25), [AuthResource.java:31](src/main/java/com/bcp/workshop/auth/AuthResource.java#L31), [AuthResource.java:42](src/main/java/com/bcp/workshop/auth/AuthResource.java#L42) | media | Pendiente

## Observaciones fuera de la matriz

- HU-01 pide que el token lleve `client` y que `expiresIn` coincida con `exp - now`. El código no hace ninguna de las dos cosas. Esto se cubrirá al corregir H-03.
- `"debug": "authenticated-with-legacy-flow"` en la respuesta exitosa es un campo informativo que no está en RETO.md. Se conserva por contrato hasta confirmar que nadie lo usa.
- Los errores de base de datos se convierten en 401 (ver [docs/ENDPOINTS.md](docs/ENDPOINTS.md)).
