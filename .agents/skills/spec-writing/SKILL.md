---
name: spec-writing
description: "Convierte una necesidad o hallazgo aprobado en una especificación verificable para un sistema existente. Úsalo para redactar requisitos, criterios de aceptación, preservar contratos y preparar una spec antes del diseño o implementación."
---

# Spec Writing

## Cuándo usar

Cuando una necesidad o hallazgo ya fue aprobado y debe transformarse en una especificación verificable y versionable antes de diseñar o implementar una solución.

## Procedimiento

1. **Confirma la entrada y el alcance.** Identifica el repositorio/componente, la necesidad o hallazgo aprobado, su estado y el resultado solicitado. Si la fuente está marcada como candidata, pendiente de aprobación o no se puede localizar, no la conviertas en requisito: señala el bloqueo y pide confirmación.
2. **Lee las fuentes relevantes antes de redactar.** Consulta la evidencia del hallazgo, la documentación funcional, los contratos y las pruebas que describan el comportamiento existente. Mantente dentro del alcance indicado; no inspecciones otros componentes salvo que la solicitud lo permita.
3. **Registra hechos con trazabilidad.** Describe el comportamiento actual solo con evidencia observada. Para cada hecho importante, indica archivo y línea o el comando ejecutado y su resultado. Distingue hechos, supuestos e inferencias; no presentes una hipótesis como requisito confirmado.
4. **Formula el problema y el resultado deseado.** Explica qué necesidad aprobada se atiende, quién o qué flujo resulta afectado y qué resultado observable debe obtenerse, sin adelantar una solución técnica.
5. **Define los límites.** Especifica qué incluye y excluye la spec. Registra expresamente los comportamientos que deben preservarse y los contratos existentes que no deben cambiar.
6. **Escribe criterios de aceptación verificables.** Usa condiciones observables, con entradas y resultados esperados. Cubre el flujo normal y los casos límite o errores relevantes que estén respaldados por la necesidad o por el contrato. Asocia cada criterio con una forma de validación y evidencia de origen.
7. **Haz visibles los vacíos.** Registra riesgos, supuestos y preguntas abiertas. No completes información ausente por intuición. Si una respuesta es necesaria para definir el comportamiento o el contrato, detén la spec como borrador y solicita esa decisión.
8. **Mantén la spec independiente del diseño.** No prescribas clases, archivos, dependencias ni pasos de implementación, excepto cuando exista una restricción técnica ya aprobada y documentada.
9. **Valida consistencia y completitud.** Comprueba que cada criterio derive de la fuente aprobada, tenga un resultado comprobable, respete los contratos protegidos y no contradiga otro criterio. Señala explícitamente lo que no pudo verificarse.
10. **Entrega para revisión humana y detente.** No modifiques código, contratos, backlog ni crees issues automáticamente. Indica si el documento es una propuesta completa o un borrador bloqueado por preguntas.

## Estructura sugerida

```markdown
# <título>

## Estado
Propuesta para revisión / Borrador bloqueado

## Fuente aprobada
Identificador, estado, motivo de aprobación y enlace o ruta

## Contexto actual
Hechos observados y evidencia concreta

## Problema confirmado

## Resultado esperado

## Comportamiento a preservar

## Alcance

## Fuera de alcance

## Criterios de aceptación
| ID | Criterio verificable | Validación | Fuente/evidencia |
|---|---|---|---|

## Contratos afectados/protegidos
Contrato, consumidor y cambio permitido o prohibido

## Riesgos, supuestos y preguntas abiertas

## Verificación de la spec
- [ ] La fuente está aprobada y trazable.
- [ ] Los hechos están separados de supuestos e hipótesis.
- [ ] Cada criterio es verificable y tiene una fuente.
- [ ] Alcance, exclusiones y contratos protegidos están explícitos.
- [ ] No se prescribe diseño no aprobado ni se propone trabajo fuera del alcance.
```

## Reglas

- No inventar requisitos, evidencia, resultados de herramientas ni estados de aprobación.
- No incluir hallazgos candidatos o no aprobados como requisitos confirmados.
- No cambiar contratos de ReqFlow implícitamente; indicar los consumidores y pedir decisión explícita si el cambio es necesario.
- No convertir la spec en diseño de clases, plan de implementación ni código.
- Usar lenguaje claro y comprobable; evitar expresiones vagas como “rápido”, “seguro” o “fácil” sin una medida acordada.
- Si falta información no bloqueante, marcarla como supuesto o pregunta abierta; si bloquea el comportamiento esperado, detenerse y pedir aclaración antes de declarar la spec lista.
- Citar las rutas de forma relativa al repositorio objetivo y no afirmar validaciones que no se ejecutaron.
