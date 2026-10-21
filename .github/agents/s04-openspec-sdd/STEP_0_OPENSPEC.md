# Paso 0 - Preparar OpenSpec para ReqFlow

## Objetivo

Instalar e inicializar OpenSpec en `workshop-requirements-api`, conservar el schema estándar `spec-driven` y dejar sus workflows disponibles para GitHub Copilot antes de iniciar el SDD guiado.

## 0.1 Prerrequisito

OpenSpec es una CLI basada en Node.js. Verifica:

```powershell
node --version
```

Se requiere Node.js 20.19.0 o superior.

## 0.2 Instalar OpenSpec CLI

```powershell
npm install -g @fission-ai/openspec@latest
openspec --version
```

## 0.3 Inicializar el repositorio

Desde la raíz de `workshop-requirements-api`:

```powershell
openspec init
```

En el selector interactivo, elige **GitHub Copilot** como herramienta de IA utilizada por el laboratorio.

OpenSpec crea su estructura de trabajo y los workflows/skills correspondientes a la herramienta seleccionada. Reinicia o refresca el host si OpenSpec lo solicita.

> No reemplaces a ciegas `openspec/config.yaml`: `openspec init` puede guardar allí configuración generada para la herramienta seleccionada. Conserva esa configuración y agrega únicamente el contexto/reglas del laboratorio que necesites.

## 0.4 Confirmar el schema estándar

El recorrido principal del Día 3 utiliza:

```text
spec-driven
```

Su flujo esperado es:

```text
proposal -> specs -> design -> tasks -> apply
```

No instales ni actives `reqflow-github` durante el recorrido principal. Ese schema se conserva en `templates/openspec/schemas/reqflow-github/` únicamente como ejemplo opcional de personalización para el cierre del día.

## 0.5 Configurar contexto ReqFlow

Usa `templates/openspec/config.yaml.example` como referencia para incorporar el contexto y reglas del laboratorio al `openspec/config.yaml` generado, **sin borrar propiedades que haya creado `openspec init`**.

La configuración debe mantener `schema: spec-driven` y recordar, entre otras cosas, que:

- el cambio parte de un GitHub Issue confirmado;
- deben preservarse contratos HTTP fuera del alcance aprobado;
- no se resuelve deuda adicional;
- los artefactos se escriben en español;
- `tasks.md` debe ser coherente con Issue, proposal, specs y design.

## 0.6 Verificar el flujo completo

El participante debe poder explicar que OpenSpec materializará, para un cambio:

```text
proposal.md
specs/
design.md
tasks.md
```

y que posteriormente `apply` utilizará esos artefactos para ejecutar el cambio.

En paralelo:

```text
GitHub Issue = trabajo comprometido y trazabilidad del producto
tasks.md      = checklist de implementación dentro del cambio OpenSpec
```

## 0.7 Publicar la configuración antes de usar cloud agent

La configuración de OpenSpec y los workflows instalados deben estar versionados y disponibles en el repositorio que utilizará GitHub Copilot cloud agent.

Antes del Paso 1 confirma que `main` contiene, como mínimo:

```text
.github/agents/
.github/skills/
openspec/config.yaml
```

además de los archivos de integración generados por `openspec init` para GitHub Copilot.

En `s01` y `s02` copiaste agents y skills a la raíz de `reqflow-workspace/`. Cópialos ahora al repo, junto a los Skills del Día 2 que ya están ahí:

```powershell
# Desde reqflow-workspace/
Copy-Item -Recurse -Force .github\agents workshop-requirements-api\.github\
Copy-Item -Recurse -Force .github\skills\* workshop-requirements-api\.github\skills\
```

> Revisa el diff de `openspec init` y conserva únicamente archivos cuyo propósito comprendas.

## 0.8 Comprobación

Ejecuta:

```powershell
openspec list
```

En este punto puede no existir todavía ningún cambio activo. Lo importante es que la CLI reconozca el proyecto y su configuración.

## Criterio de salida

Puede demostrarse que:

- OpenSpec está instalado;
- `workshop-requirements-api` está inicializado;
- GitHub Copilot fue seleccionado como herramienta;
- el recorrido principal usa el schema estándar `spec-driven`;
- el flujo incluye `proposal`, `specs`, `design`, `tasks` y `apply`;
- GitHub Issues continúan formando parte del proceso.
