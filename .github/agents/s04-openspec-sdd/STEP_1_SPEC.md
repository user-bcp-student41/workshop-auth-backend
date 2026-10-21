# Paso 1 - Crear proposal y specs desde el GitHub Issue

## Objetivo

Transformar un GitHub Issue confirmado en artefactos OpenSpec que definan por qué se hace el cambio y qué comportamiento debe preservarse o modificarse.

## Precondición

Antes de comenzar este paso debe existir un GitHub Issue de `workshop-requirements-api` seleccionado para el recorrido guiado.

Ese Issue se crea durante el Discovery y backlog del módulo anterior, en [S03 - Paso 4: Discovery y backlog multi-repo](../s03-reqflow-setup/STEP_4_DISCOVERY_BACKLOG.md):

- **4.5** Crear issues a partir de hallazgos CONFIRMADOS;
- **4.6** agregar los issues al Project ReqFlow;
- **4.7** elegir un issue de `workshop-requirements-api` para el recorrido guiado de S4.

El Issue seleccionado debe corresponder a un hallazgo **CONFIRMADO** y será la fuente de alcance y trazabilidad para todo el flujo SDD de este módulo.

## 1.1 Preparar la rama del Issue

Usa el Issue seleccionado en S3 para crear una rama de desarrollo **vinculada al Issue y dejarla activa localmente**.

Desde `reqflow-workspace`:

```powershell
cd workshop-requirements-api

gh issue develop <ISSUE_NUMBER> --checkout --base main --name <BRANCH_DEL_ISSUE>
```

Por ejemplo:

```powershell
gh issue develop 9 --checkout --base main --name feature/issue-9-auth-legacy-base64
```

Este comando realiza en una sola operación el flujo que necesitamos:

```text
GitHub Issue
 -> crea rama vinculada al Issue
 -> publica/configura la rama de desarrollo
 -> checkout local
```

Verifica únicamente que quedaste sobre la rama esperada:

```powershell
git branch --show-current
```

El resultado debe ser exactamente `<BRANCH_DEL_ISSUE>`.

> Si ya creaste la rama desde la sección **Development** del Issue en GitHub, no crees otra. En ese caso basta con sincronizarla y cambiar a ella:
>
> ```powershell
> git fetch origin
> git switch <BRANCH_DEL_ISSUE>
> ```

Esa rama será la rama del Issue utilizada durante todo el recorrido principal de S4.

Para S4 mantén VS Code abierto desde `reqflow-workspace` para conservar el MCP configurado a nivel workspace. Aunque el workspace contiene varios repositorios, todos los prompts y operaciones de este laboratorio deben limitarse explícitamente a `workshop-requirements-api`.

## 1.2 Qué debe producir OpenSpec

Para este paso esperamos:

```text
openspec/changes/<change-name>/
├─ .openspec.yaml
├─ proposal.md
└─ specs/
   └─ <capability>/
      └─ spec.md
```

Todavía no queremos design, tasks ni implementación.

## 1.3 Prompt recomendado

En GitHub Copilot Chat de VS Code, abre el selector **Agent** y elige:

```text
PM/PO
```

Prompt sugerido:

```text
Trabaja únicamente en workshop-requirements-api.
No leas ni modifiques los otros repositorios del workspace.

Trabaja sobre la rama actual.
No crees ni cambies de rama.

Usa github-official para consultar el GitHub Issue #<ISSUE_NUMBER>
y úsalo como fuente del trabajo comprometido y del alcance.

Trabaja con OpenSpec y el schema estándar spec-driven configurado en este repositorio.

Usa github-official para consultar el Issue y úsalo como fuente del trabajo comprometido y del alcance.
Usa docs/ENDPOINTS.md (contrato documentado el Dia 1) como baseline del comportamiento que debe preservarse: los escenarios de las specs deben ser coherentes con ese contrato, o explicar por que el Issue lo cambia.

Crea una propuesta OpenSpec para este cambio y materializa:
- proposal.md;
- las specs necesarias bajo openspec/changes/<change>/specs/.

La propuesta debe:
- referenciar el Issue;
- explicar por qué importa el cambio;
- separar alcance y fuera de alcance;
- identificar comportamiento que debe preservarse;
- definir requisitos verificables y escenarios observables.

No diseñes todavía la implementación técnica.
No crees tasks.md todavía.
No modifiques código funcional.
```

## 1.4 Validar la estructura

Verifica que:

- exista `proposal.md`;
- exista al menos una spec cuando el cambio modifica comportamiento observable;
- todavía no exista `design.md` ni `tasks.md`;
- el proposal referencia el GitHub Issue correcto;
- no haya código funcional modificado.

Ejecuta los comandos OpenSpec con `workshop-requirements-api` como directorio de trabajo.

Primero identifica el nombre exacto del cambio:

```powershell
openspec list
```

También puedes verlo como el nombre de la carpeta bajo `openspec/changes/` del repositorio `workshop-requirements-api`.

Por ejemplo:

```text
openspec/changes/issue-<N>-<slug-del-hallazgo>/
```

Entonces:

```text
CHANGE_NAME = issue-<N>-<slug-del-hallazgo>
```

Con ese nombre, ejecuta o solicita evidencia de:

```powershell
openspec status --change <CHANGE_NAME>
openspec validate <CHANGE_NAME>
```

Ejemplo:

```powershell
openspec status --change <CHANGE_NAME>
openspec validate <CHANGE_NAME>
```

## 1.5 Gate humano

Revisa proposal y specs:

- ¿es el problema correcto?;
- ¿el alcance coincide con el Issue?;
- ¿queda explícito lo que no cambia?;
- ¿los requisitos son verificables?;
- ¿los escenarios protegen contratos reales?;
- ¿no se agregó deuda secundaria?

## 1.6 Integrar la etapa

Si proposal y specs son correctos, registra esta etapa con un commit sobre la misma rama vinculada al Issue.

Antes de commitear, revisa el diff y confirma que solo contiene los artefactos esperados de esta etapa dentro de `workshop-requirements-api`.

Puedes usar GitHub Copilot en VS Code para generar el mensaje del commit a partir de los cambios staged.

Ejemplo conceptual:

```text
docs(openspec): define issue 10 proposal and specs

- referencia GitHub Issue #10
- agrega proposal y specs del cambio
- valida el change de OpenSpec
- no modifica código funcional
```

No abras todavía un PR hacia `main`. La rama del Issue seguirá acumulando las siguientes etapas del flujo SDD.

## Criterio de salida

La rama integradora contiene un cambio OpenSpec válido con `proposal.md` y `specs/`, vinculado al GitHub Issue y sin implementación funcional.
