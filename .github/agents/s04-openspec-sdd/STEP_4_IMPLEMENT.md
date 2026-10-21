# Paso 4 - Implementar con OpenSpec apply + GitHub Issue

## Objetivo

Implementar únicamente el GitHub Issue aprobado usando el cambio OpenSpec completo y `tasks.md` como checklist de ejecución.

## 4.1 Partir de la rama integradora actualizada

Confirma que estás trabajando sobre `<BRANCH_DEL_ISSUE>` de `workshop-requirements-api` y que allí existen:

- OpenSpec proposal aprobado;
- OpenSpec specs aprobadas;
- OpenSpec design aprobado;
- OpenSpec tasks aprobado;
- tareas de caracterización completadas;
- baseline conocido.

Continúa trabajando sobre la misma rama del Issue. No crees ni cambies a otra rama para esta etapa.

En GitHub Copilot Chat, abre el selector **Agent** y elige:

```text
Developer
```

## 4.2 Prompt recomendado

```text
Trabaja únicamente en workshop-requirements-api.
No leas ni modifiques los otros repositorios del workspace.
Todos los comandos, lecturas, cambios y operaciones Git/GitHub de este paso deben referirse únicamente a workshop-requirements-api.

Trabaja sobre la rama actual vinculada al GitHub Issue. No crees ni cambies de rama.

Implementa el GitHub Issue seleccionado utilizando el cambio OpenSpec <CHANGE_NAME> como definición y plan aprobados.

Fuentes de verdad:
1. GitHub Issue aprobado;
2. openspec/changes/<CHANGE_NAME>/proposal.md;
3. openspec/changes/<CHANGE_NAME>/specs/;
4. openspec/changes/<CHANGE_NAME>/design.md;
5. openspec/changes/<CHANGE_NAME>/tasks.md;
6. pruebas de caracterización existentes y su baseline.

Usa el workflow OpenSpec apply disponible para GitHub Copilot.

Reglas:
- implementa siguiendo las tareas pendientes de tasks.md;
- marca una tarea como completada solo cuando exista evidencia de que terminó;
- no amplíes el scope fuera del Issue y OpenSpec;
- no cambies contratos fuera del alcance;
- no resuelvas hallazgos secundarios;
- no desactives tests ni controles;
- realiza cambios pequeños;
- ejecuta las pruebas relevantes después de cada cambio significativo;
- registra comandos y resultados;
- detente si implementación, spec, design y tasks entran en conflicto.
```

## 4.3 Qué significa `apply`

En el recorrido principal usamos el schema estándar `spec-driven`:

```text
proposal -> specs -> design -> tasks -> apply
```

Durante `apply`, OpenSpec guía la implementación a partir de los artefactos aprobados y del estado de `tasks.md`.

GitHub conserva, en paralelo:

```text
Issue   = unidad de trabajo y trazabilidad del producto
Project = prioridad y coordinación
PR      = revisión e integración
```

## 4.4 Validación

El agente debe ejecutar, con `workshop-requirements-api` como directorio de trabajo y según corresponda al repositorio real:

- tests de caracterización;
- tests del cambio;
- build/lint disponibles;
- pruebas HTTP relevantes;
- `openspec validate <CHANGE_NAME>` si los artefactos OpenSpec fueron modificados.

Antes de cerrar la etapa revisa `tasks.md`: cualquier checkbox marcado debe corresponder a trabajo realmente ejecutado y validado.

## 4.5 Integrar la etapa

Antes de registrar los cambios, revisa:

- el diff de implementación dentro de `workshop-requirements-api`;
- `tasks.md` y el estado real de cada checkbox;
- las pruebas ejecutadas y sus resultados;
- que cada tarea marcada como completada tenga evidencia;
- que no existan cambios fuera del alcance aprobado.

Registra los cambios directamente en `<BRANCH_DEL_ISSUE>` con un commit descriptivo. Puedes usar GitHub Copilot para proponer el mensaje del commit.

Por ejemplo:

```text
feat: implement approved <CHANGE_NAME> change
```

El commit debe incluir la implementación validada y la actualización correspondiente de `tasks.md`.

No abras todavía el PR final hacia `main`; ese paso corresponde al Step 5.

## Criterio de salida

`<BRANCH_DEL_ISSUE>` contiene la implementación aprobada del GitHub Issue, satisface las specs OpenSpec relevantes, refleja correctamente el progreso en `tasks.md` y mantiene pruebas reproducibles en verde según el baseline esperado.
