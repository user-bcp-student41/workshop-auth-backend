# Paso 2 - Crear design y tasks con OpenSpec

## Objetivo

Convertir proposal y specs aprobados en un diseño técnico incremental y luego descomponer ese diseño en `tasks.md` usando el flujo estándar de OpenSpec.

## 2.1 Partir de la rama integradora actualizada

Confirma que estás trabajando sobre `<BRANCH_DEL_ISSUE>` de `workshop-requirements-api` y que el commit del Paso 1 ya contiene:

```text
openspec/changes/<CHANGE_NAME>/proposal.md
openspec/changes/<CHANGE_NAME>/specs/
```

Continúa trabajando sobre esa misma rama. En GitHub Copilot Chat, abre el selector **Agent** y elige:

```text
Architect
```

## 2.2 Crear el design

Prompt recomendado:

```text
Trabaja únicamente en workshop-requirements-api.
No leas ni modifiques los otros repositorios del workspace.
Todos los comandos, lecturas, cambios y operaciones Git/GitHub de este paso deben referirse únicamente a workshop-requirements-api.

Trabaja sobre la rama actual <BRANCH_DEL_ISSUE>. No crees ni cambies a otra rama.

Trabaja sobre el cambio OpenSpec <CHANGE_NAME> ya aprobado.
Lee proposal.md y specs/ antes de proponer cualquier diseño.

Crea o completa:
openspec/changes/<CHANGE_NAME>/design.md

El design debe incluir:
- contexto técnico relevante;
- goals / non-goals;
- contratos protegidos;
- componentes afectados;
- decisiones técnicas y alternativas consideradas;
- estrategia incremental;
- pruebas de caracterización necesarias antes de modificar comportamiento crítico;
- riesgos / trade-offs;
- rollback cuando aplique.

No implementes código funcional.
No rediseñes toda la aplicación.
```

## 2.3 Gate humano del design

Revisa:

- ¿cumple las specs sin ampliar alcance?;
- ¿protege contratos existentes?;
- ¿explica decisiones y trade-offs?;
- ¿identifica qué comportamiento debe caracterizarse antes de cambiar?;
- ¿evita una reescritura innecesaria?

Si una pregunta cambiaría lo que se construirá, resuélvela antes de avanzar.

## 2.4 Crear `tasks.md`

Una vez aprobado el design, completa el artefacto `tasks.md` del mismo cambio OpenSpec.

Prompt recomendado:

```text
Trabaja únicamente en workshop-requirements-api.
No leas ni modifiques los otros repositorios del workspace.
Todos los comandos, lecturas, cambios y operaciones Git/GitHub de este paso deben referirse únicamente a workshop-requirements-api.

Continúa sobre la rama actual <BRANCH_DEL_ISSUE>. No crees ni cambies de rama.

Usa proposal.md, specs/ y design.md ya aprobados del cambio OpenSpec <CHANGE_NAME>.

Genera o completa:
openspec/changes/<CHANGE_NAME>/tasks.md

Las tareas deben:
- estar dentro del alcance del GitHub Issue;
- ser pequeñas y verificables;
- comenzar por las pruebas de caracterización necesarias antes del cambio funcional;
- incluir la implementación incremental;
- incluir pruebas/validaciones relevantes;
- evitar trabajo secundario no aprobado.

El GitHub Issue sigue siendo la unidad de trabajo del producto. tasks.md es el checklist de implementación de este cambio OpenSpec.

No implementes todavía el cambio funcional.
```

## 2.5 Validar OpenSpec

Ejecuta los comandos OpenSpec con `workshop-requirements-api` como directorio de trabajo.

Si no recuerdas el nombre del cambio, puedes obtenerlo con:

```powershell
openspec list
```

También corresponde al nombre de la carpeta bajo:

```text
openspec/changes/
```

Con el nombre identificado, ejecuta:

```powershell
openspec status --change <CHANGE_NAME>
openspec validate <CHANGE_NAME>
```

Comprueba que el cambio contiene:

```text
proposal.md
specs/
design.md
tasks.md
```

y que está listo para `apply` según el schema `spec-driven`.

## 2.6 Gate humano de tasks

Revisa:

- ¿cada tarea deriva del Issue, specs y design?;
- ¿la caracterización ocurre antes de modificar comportamiento crítico?;
- ¿las tareas son verificables?;
- ¿no aparecen refactors o mejoras fuera de alcance?;
- ¿el checklist permite saber qué falta durante `apply`?

## 2.7 Integrar la etapa

Una vez aprobados `design.md` y `tasks.md`, registra los cambios directamente en `<BRANCH_DEL_ISSUE>` de `workshop-requirements-api`.

Revisa el diff y crea un commit descriptivo que deje claro que esta etapa incorpora el diseño y el plan de implementación del cambio OpenSpec. Puedes usar GitHub Copilot para proponer el mensaje del commit.

Por ejemplo:

```text
docs(openspec): add design and implementation tasks
```

No abras todavía un PR hacia `main`.

## Criterio de salida

`<BRANCH_DEL_ISSUE>` contiene un cambio OpenSpec válido con proposal, specs, design y tasks aprobados y registrados en la propia rama del Issue. El GitHub Issue y `tasks.md` son coherentes aunque cumplan funciones de tracking distintas.
