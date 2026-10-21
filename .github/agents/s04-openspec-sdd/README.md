# Módulo práctico integrador - SDD con OpenSpec sobre `workshop-requirements-api`

> Este módulo materializa principalmente **S1 - OpenSpec / Spec Kit + Constitution**, **S2 - Specify -> Plan -> Tasks -> Validate** y los gates/evaluators de **S4 - routing, evaluator, HITL**.

## Objetivo

Demostrar un ciclo completo de Spec Driven Development sobre un sistema existente usando OpenSpec como implementación concreta y manteniendo GitHub como sistema de trabajo, trazabilidad e integración.

Repositorio guiado:

```text
<OWNER>/workshop-requirements-api
```

VS Code permanece abierto sobre `reqflow-workspace`, pero todos los prompts operativos de este módulo delimitan explícitamente el trabajo a `workshop-requirements-api`.

## Relación con S1

OpenSpec es la implementación SDD usada en el laboratorio.

Spec Kit y Constitution forman parte de la comparación conceptual de S1; no ejecutamos un segundo flujo paralelo sobre el mismo cambio.

## Relación con S2

Patrón del temario:

```text
Specify -> Plan -> Tasks -> Validate
```

Materialización principal:

```text
Specify
  -> proposal.md + specs/

Plan
  -> design.md

Tasks
  -> tasks.md

Validate
  -> openspec validate + revisión humana + tests

Apply
  -> implementar las tareas aprobadas
```

## Decisión del laboratorio

Usamos el schema estándar de OpenSpec:

```text
proposal -> specs -> design -> tasks -> apply
```

Y mantenemos GitHub en paralelo:

```text
GitHub Issue   -> unidad de trabajo y trazabilidad
GitHub Project -> prioridad
branch         -> rama del Issue
PR             -> revisión e integración
checks         -> evidencia automática
```

`tasks.md` y GitHub Issue pueden solaparse parcialmente. Durante el taller esa duplicación es intencional.

## Preparación

Antes del flujo del Issue:

1. instala OpenSpec CLI;
2. inicializa OpenSpec en `workshop-requirements-api` seleccionando GitHub Copilot;
3. conserva el schema estándar `spec-driven`;
4. configura el contexto y reglas del laboratorio;
5. trabaja desde `reqflow-workspace`, manteniendo el alcance operativo en el repositorio API.

Consulta [Paso 0 - Preparar OpenSpec](STEP_0_OPENSPEC.md).

## Flujo

```text
Issue confirmado y priorizado
 -> rama del Issue
 -> proposal + specs              [Specify]
 -> GATE HUMANO
 -> design                        [Plan]
 -> GATE HUMANO
 -> tasks.md                      [Tasks]
 -> GATE HUMANO
 -> openspec validate
 -> characterization tests        [Validate baseline]
 -> OpenSpec apply
 -> implementación incremental
 -> tests / evaluators            [Validate change]
 -> PR final rama del Issue -> main
 -> GitHub checks
 -> Definition of Done
 -> GATE HUMANO de cierre
```

No se crean PRs internos `copilot/*` en el recorrido principal de VS Code.

## Routing, evaluator y HITL dentro del módulo

### Routing

```text
PM/PO     -> necesidad, alcance, proposal/specs
Architect -> design/tasks
Developer -> characterization, apply e implementación
```

### Evaluator

```text
openspec validate
tests
diff
Definition of Done
GitHub checks
```

### HITL

El participante revisa y decide antes de aceptar cada artefacto relevante y antes de integrar el cambio.

## Pasos

0. [Preparar OpenSpec](STEP_0_OPENSPEC.md)
1. [Crear proposal y specs desde el Issue](STEP_1_SPEC.md)
2. [Crear design y tasks](STEP_2_ARCHITECTURE.md)
3. [Proteger el comportamiento](STEP_3_CHARACTERIZATION.md)
4. [Implementar con OpenSpec apply](STEP_4_IMPLEMENT.md)
5. [Preparar PR final y revisar quality gates](STEP_5_PR_CI.md)

## Ejecución alternativa desde GitHub Copilot Agents

El recorrido principal continúa validado desde VS Code sobre `reqflow-workspace`.

Como complemento, determinadas tareas pueden ejecutarse desde GitHub Copilot Agents cuando los Custom Agents, Skills y configuración requerida ya están versionados y disponibles para GitHub. Esa ruta puede tener reglas distintas de ramas/sesiones y se documenta separadamente.

Consulta [Ejecución alternativa desde GitHub Copilot Agents](GITHUB_COPILOT_AGENTS.md).

## Personalización al final del día

Después de completar el flujo estándar, revisamos `templates/openspec/schemas/reqflow-github/` como ejemplo opcional de personalización. Ese schema elimina `tasks.md` y deja el tracking en GitHub Issues.

La personalización cambia el proceso y los artefactos de OpenSpec; no cambia la funcionalidad de ReqFlow.

## Criterio de salida

El participante puede mostrar:

```text
Specify
-> Plan
-> Tasks
-> Validate
-> Apply
-> Validate
-> PR / checks
```

sobre un Issue real de ReqFlow, y puede identificar dónde aparecen routing, evaluator y HITL en el recorrido.
