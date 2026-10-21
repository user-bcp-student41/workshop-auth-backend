# Paso 3 - Proteger el comportamiento antes de implementar

## Objetivo

Ejecutar primero las tareas de caracterización definidas en `tasks.md` para crear evidencia del comportamiento que debe preservarse antes de modificar una pieza crítica.

## 3.1 Partir de la rama integradora actualizada

Confirma que estás trabajando sobre `<BRANCH_DEL_ISSUE>` de `workshop-requirements-api` y que el cambio OpenSpec ubicado en:

```text
openspec/changes/<CHANGE_NAME>/
```

contiene:

```text
proposal.md
specs/
design.md
tasks.md
```

Valida además que el cambio sigue siendo válido, ejecutando OpenSpec con `workshop-requirements-api` como directorio de trabajo:

```powershell
openspec validate <CHANGE_NAME>
```

Continúa trabajando sobre la misma rama del Issue. No crees ni cambies a otra rama para esta etapa.

En GitHub Copilot Chat, abre el selector **Agent** y elige:

```text
Developer
```

## 3.2 Prompt recomendado

```text
Trabaja únicamente en workshop-requirements-api.
No leas ni modifiques los otros repositorios del workspace.
Todos los comandos, lecturas, cambios y operaciones Git/GitHub de este paso deben referirse únicamente a workshop-requirements-api.

Trabaja sobre la rama actual vinculada al GitHub Issue. No crees ni cambies de rama.

Lee el GitHub Issue y el cambio OpenSpec <CHANGE_NAME> completo:
- proposal.md;
- specs/;
- design.md;
- tasks.md.

Todavía no implementes el cambio funcional.

Ejecuta únicamente las tareas de tasks.md relacionadas con caracterización y protección del comportamiento existente.

Antes de crear pruebas nuevas, revisa docs/TESTING.md (Dia 1): reutiliza las
que ya protegen el comportamiento y agrega solo las que falten.

Crea las pruebas de caracterización necesarias y ejecútalas.

Reglas:
- no corrijas todavía el problema objeto del Issue;
- no cambies contratos HTTP;
- no amplíes cobertura fuera del scope;
- explica qué requisito o comportamiento protege cada prueba;
- registra el comando ejecutado y su resultado;
- actualiza en tasks.md únicamente las tareas de caracterización realmente completadas;
- si una prueba demuestra que proposal, specs, design o tasks eran incorrectos, detente y solicita revisión.
```

> En esta etapa el Developer puede modificar únicamente las pruebas necesarias y `tasks.md` dentro de `workshop-requirements-api`. Puede ejecutar la suite para obtener una línea base reproducible, pero todavía no debe modificar código funcional.

En PowerShell, si el proyecto necesita agregar la raíz del repositorio al path de imports, ejecuta el comando con `workshop-requirements-api` como directorio de trabajo, por ejemplo:

```powershell
$env:PYTHONPATH='.'; python -m pytest tests/test_api.py -q
```

Un error de entorno o de invocación del comando no equivale a un fallo del comportamiento caracterizado. Corrige primero el entorno y vuelve a ejecutar la misma batería de pruebas antes de sacar conclusiones sobre el código.

## 3.3 Relación con OpenSpec

En una aplicación brownfield, OpenSpec define el cambio, pero antes de aplicar tareas funcionales necesitamos proteger el comportamiento existente que podría verse afectado. Por eso este laboratorio introduce un gate de characterization tests antes del cambio funcional.

```text
spec
= comportamiento requerido

design
= estrategia técnica

tasks.md
= plan/checklist aprobado

characterization test
= evidencia ejecutable del comportamiento existente que debemos proteger
```

Las pruebas de caracterización forman parte de la ejecución del plan, pero se separan como gate brownfield antes del cambio funcional.

## 3.4 Evidencia esperada

La sesión debe dejar evidencia de:

```text
comando ejecutado
resultado
número de tests relevantes
spec/requisito relacionado
tarea de tasks.md relacionada
comportamiento protegido
```

## 3.5 Integrar la etapa

Una vez que las pruebas de caracterización pasan y `tasks.md` refleja únicamente las tareas realmente completadas, revisa el diff antes de registrar los cambios.

Confirma que:

- solo se modificaron pruebas necesarias para caracterizar el baseline y `tasks.md` dentro de `workshop-requirements-api`;
- no se modificó código funcional;
- las pruebas ejecutadas pasan sobre el comportamiento actual;
- las tareas futuras que todavía no corresponden al baseline permanecen pendientes.

Registra la etapa directamente en `<BRANCH_DEL_ISSUE>` con un commit descriptivo. Puedes usar GitHub Copilot para proponer el mensaje del commit.

Por ejemplo:

```text
test: characterize current <comportamiento-protegido> behavior
```

El commit debe incluir las pruebas de caracterización y la actualización correspondiente de `tasks.md`.

No abras todavía un PR hacia `main`.

## Criterio de salida

`<BRANCH_DEL_ISSUE>` contiene proposal, specs, design y tasks aprobados, con las tareas de caracterización realmente verificadas marcadas como completadas y una red de seguridad reproducible antes del cambio funcional.
