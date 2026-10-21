# Paso 5 - Preparar Pull Request final y revisar quality gates

## Objetivo

Cerrar el ciclo guiado con evidencia suficiente para una revisión independiente y con un Pull Request final desde la rama del Issue hacia `main`.

## 5.1 Confirmar la rama del Issue

Confirma que estás trabajando sobre `<BRANCH_DEL_ISSUE>` de `workshop-requirements-api` y que allí existen:

- `proposal.md` aprobado;
- `specs/` aprobadas;
- `design.md` aprobado;
- `tasks.md` actualizado;
- pruebas de caracterización;
- implementación del Issue;
- evidencia de pruebas relevantes.

Valida además el cambio OpenSpec con `workshop-requirements-api` como directorio de trabajo:

```powershell
openspec validate <CHANGE_NAME>
```

Revisa `tasks.md`: no debe haber tareas marcadas como completas sin evidencia. Si quedan tareas pendientes, determina si bloquean el cierre o si el alcance debe volver a revisión.

## 5.2 Preparar el contenido del PR

Si trabajas desde el workspace multi-repo, limita explícitamente el contexto al repositorio objetivo:

```text
Trabaja únicamente en workshop-requirements-api.
No leas ni modifiques los otros repositorios del workspace.
```

En GitHub Copilot Chat, abre el selector **Agent**, elige `Developer` y usa un prompt como el siguiente:

```text
Trabaja únicamente en workshop-requirements-api.
No leas ni modifiques los otros repositorios del workspace.
Todos los comandos, lecturas, cambios y operaciones Git/GitHub de este paso deben referirse únicamente a workshop-requirements-api.

Prepara el Pull Request final del GitHub Issue desde la rama actual <BRANCH_DEL_ISSUE> hacia main.

Usa únicamente evidencia existente en:
- el GitHub Issue;
- openspec/changes/<CHANGE_NAME>/proposal.md;
- openspec/changes/<CHANGE_NAME>/specs/;
- openspec/changes/<CHANGE_NAME>/design.md;
- openspec/changes/<CHANGE_NAME>/tasks.md;
- diff acumulado;
- pruebas ejecutadas.

Antes de preparar el PR:
- verifica que estás trabajando con <BRANCH_DEL_ISSUE>;
- revisa que tasks.md refleje el trabajo realmente completado;
- revisa el diff acumulado;
- confirma que las pruebas relevantes tienen evidencia.

Prepara:
- título;
- descripción;
- Issue relacionado;
- cambio OpenSpec relacionado;
- estado de tasks.md;
- comportamiento protegido;
- cambios realizados;
- pruebas ejecutadas y resultados;
- trade-offs;
- riesgos residuales;
- rollback;
- trabajo fuera de alcance.

No crees ramas adicionales.
No modifiques el código en esta etapa.
```

El objetivo de este paso es preparar un PR auditable a partir de evidencia existente, no volver a implementar el cambio.

## 5.3 Crear el PR final

El Pull Request final propone integrar todo el trabajo del Issue desde `<BRANCH_DEL_ISSUE>` hacia `main`.

En GitHub, la dirección debe ser:

```text
base: main
compare: <BRANCH_DEL_ISSUE>
```

Donde:

```text
base    = rama destino
compare = rama que contiene los cambios
```

Antes de crear el PR revisa:

- commits incluidos;
- archivos modificados;
- diff acumulado;
- que no haya cambios fuera del Issue;
- que `tasks.md` refleje el estado real;
- que las pruebas relevantes tengan evidencia.

Usa como título y descripción el contenido preparado en el paso anterior.

Si el agente dispone de acceso a GitHub mediante MCP, puede realizar la operación sobre el PR de `workshop-requirements-api`. Si la creación se hace manualmente en GitHub.com, conserva exactamente la misma dirección de ramas y revisa el diff antes de confirmar.

## 5.4 Revisar trazabilidad

La cadena esperada es:

```text
Discovery
-> GitHub Issue
-> GitHub Project
-> rama del Issue
-> OpenSpec proposal/specs/design/tasks
-> characterization tests
-> OpenSpec apply
-> implementación
-> PR final rama del Issue -> main
```

Comprueba que el PR final permite reconstruir esa historia sin depender de información implícita.

## 5.5 Revisar checks automatizados

GitHub puede ejecutar automáticamente workflows y controles ya configurados en el repositorio.

En S4 no creamos ni modificamos el pipeline de CI. Revisa cada check existente como quality gate independiente.

Si alguno falla:

- no lo deshabilites para obtener verde;
- identifica el workflow/control;
- determina si el fallo fue causado por el cambio, es preexistente o corresponde a configuración del laboratorio;
- registra el resultado como evidencia;
- no confundas un fallo técnico del workflow con un fallo funcional sin investigar su causa.

Un PR mergeado no implica automáticamente que la Definition of Done esté satisfecha.

## 5.6 Definition of Done

La Definition of Done es un gate de evaluación, no un paso de implementación.

En GitHub Copilot Chat, abre el selector **Agent**, elige `Developer` y usa el Skill `reqflow-definition-of-done` para evaluar únicamente evidencia existente.

Prompt recomendado:

```text
Trabaja únicamente en workshop-requirements-api.
No leas ni modifiques los otros repositorios del workspace.
Todos los comandos, lecturas y operaciones Git/GitHub de esta evaluación deben referirse únicamente a workshop-requirements-api.

Usa el Skill reqflow-definition-of-done para evaluar el cambio actual.

Evalúa únicamente con evidencia existente en:
- el GitHub Issue;
- el Pull Request final;
- openspec/changes/<CHANGE_NAME>/proposal.md;
- openspec/changes/<CHANGE_NAME>/specs/;
- openspec/changes/<CHANGE_NAME>/design.md;
- openspec/changes/<CHANGE_NAME>/tasks.md;
- pruebas ejecutadas;
- checks disponibles en GitHub.

Para cada criterio clasifica el resultado como:
- CUMPLIDO;
- PENDIENTE DE OTRO GATE;
- NO APLICA AL ALCANCE;
- FALTA EVIDENCIA;
- BLOQUEADO.

No declares un criterio como cumplido si no existe evidencia verificable.
No modifiques código ni archivos.
Devuélveme una tabla con:
criterio | estado | evidencia | observación
```

Interpreta los estados así:

```text
CUMPLIDO
= existe evidencia suficiente

FALTA EVIDENCIA
= podría estar bien, pero no puede demostrarse

BLOQUEADO
= existe una señal concreta que impide aprobar el criterio

NO APLICA AL ALCANCE
= el criterio no corresponde al cambio evaluado
```

No conviertas ausencia de evidencia en aprobación.

## 5.7 ¿Cuándo archivar OpenSpec?

Mientras el cambio está activo, sus artefactos viven en:

```text
openspec/changes/<CHANGE_NAME>/
```

Archivar significa declarar que ese cambio dejó de ser trabajo activo porque fue implementado, validado e integrado. El archive también puede actualizar las specs canónicas según el workflow OpenSpec utilizado.

La secuencia recomendada es:

```text
implementación terminada
-> pruebas y quality gates revisados
-> PR final aprobado y mergeado
-> Definition of Done aceptada
-> archive del cambio OpenSpec
```

No archives un cambio únicamente porque el PR exista o haya sido mergeado. Si hay gates bloqueados, tareas pendientes o falta evidencia relevante, el cambio todavía necesita revisión.

El archive puede realizarse como cierre posterior del Issue o como un PR/commit de housekeeping según la política del equipo.

## 5.8 Cierre conceptual: personalizar OpenSpec

Este paso es únicamente de comparación conceptual. No modifiques el cambio actual ni cambies el schema activo.

Compara el recorrido estándar usado en el laboratorio con el ejemplo personalizado disponible en:

```text
templates/openspec/schemas/reqflow-github/
```

El objetivo es observar que un equipo puede personalizar OpenSpec para eliminar `tasks.md` y delegar el tracking operativo a GitHub Issues.

Comparación:

```text
Recorrido principal:
proposal -> specs -> design -> tasks -> apply
                 + GitHub Issue

Alternativa personalizada:
proposal -> specs -> design -> apply
                 + GitHub Issue como tracking
```

La segunda opción puede evitar duplicación en un equipo real, pero se presenta **después** de aprender el flujo estándar completo. Cambia el proceso de trabajo, no la funcionalidad de ReqFlow.

## Criterio de salida

Existe un PR auditable desde `<BRANCH_DEL_ISSUE>` hacia `main`, conectado a un GitHub Issue y respaldado por proposal, specs, design, tasks, pruebas y quality gates revisados. El participante además puede explicar cómo OpenSpec podría personalizarse posteriormente para delegar el tracking en GitHub Issues.
