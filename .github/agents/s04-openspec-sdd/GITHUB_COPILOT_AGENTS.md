# Ejecución alternativa desde GitHub Copilot Agents

El flujo principal de S4 se ejecuta desde **VS Code**, abriendo `reqflow-workspace` y limitando cada prompt y operación al repositorio `workshop-requirements-api`.

Como alternativa, aquellas tareas cuyo contexto ya esté versionado en GitHub pueden delegarse a **GitHub Copilot Agents** desde GitHub.com.

Esta alternativa no reemplaza el recorrido principal ni los pasos que dependen explícitamente de la CLI de OpenSpec.

## Qué cambia al ejecutar desde GitHub

### 1. Los Custom Agents deben estar disponibles en la rama por defecto

Los Custom Agents que se quieran utilizar desde GitHub deben estar versionados en el repositorio y disponibles desde la rama por defecto, normalmente `main`.

Por ejemplo:

```text
.github/
└── agents/
    ├── pm-po.agent.md
    ├── architect.agent.md
    ├── developer.agent.md
    └── discovery.agent.md
```

Esto es diferente del trabajo local sobre una rama de laboratorio: GitHub necesita poder descubrir las definiciones de los agentes desde el repositorio.

### 2. GitHub Copilot trabaja sobre su propia rama `copilot/*`

Al iniciar una tarea desde GitHub Copilot Agent se crea una rama de trabajo administrada por la sesión, normalmente con el prefijo:

```text
copilot/...
```

Por ejemplo:

```text
<BASE_BRANCH>
   \
    copilot/implement-validation-rule
```

Por tanto, un prompt pensado para GitHub Copilot Agent **no debe pedir** que el agente cree o cambie de rama mediante instrucciones como:

```text
git checkout -b feature/...
git checkout ...
git switch ...
```

El agente debe trabajar sobre la rama proporcionada por la sesión.

### 3. La rama base sigue siendo una decisión del flujo

La existencia de los agentes en `main` no significa que todas las implementaciones deban partir de `main`.

Al iniciar una tarea se debe seleccionar la rama base que corresponda al flujo que se está ejecutando.

Conceptualmente:

```text
<BASE_BRANCH>
   \
    copilot/<task>
             |
             └── Pull Request -> <BASE_BRANCH>
```

Por tanto, hay que distinguir entre:

```text
main
└── rama desde la que GitHub puede descubrir agentes e instrucciones versionadas

<BASE_BRANCH>
└── rama seleccionada como punto de partida de una tarea concreta
```

### 4. Cada tarea se ejecuta sobre un repositorio concreto

En VS Code, `reqflow-workspace` proporciona contexto sobre varios repositorios:

```text
workshop-requirements-api
workshop-auth-backend
workshop-login-frontend
```

Desde GitHub Copilot Agents, cada tarea se inicia sobre un repositorio concreto.

Por ejemplo:

```text
Task A -> workshop-requirements-api
Task B -> workshop-auth-backend
Task C -> workshop-login-frontend
```

El prompt debe asumir ese límite y no intentar modificar otros repositorios del producto.

### 5. El contexto necesario debe estar versionado

Para que el agente pueda tomar una tarea desde GitHub, las fuentes de contexto que necesite deben estar disponibles en el repositorio, por ejemplo:

```text
openspec/
docs/
.github/
  agents/
  copilot-instructions.md
AGENTS.md
```

Las especificaciones, decisiones de arquitectura e instrucciones relevantes deben formar parte del contexto versionado que el agente pueda leer.

### 6. Los comandos explícitos de OpenSpec permanecen en local

Los pasos del laboratorio que requieren ejecutar comandos como:

```powershell
openspec list
openspec status --change <CHANGE_NAME>
openspec validate <CHANGE_NAME>
```

siguen ejecutándose desde VS Code con `workshop-requirements-api` como directorio de trabajo.

Una vez que los artefactos resultantes están versionados, las tareas posteriores que ya dispongan de suficiente contexto pueden delegarse a GitHub Copilot Agents.

Conceptualmente:

```text
VS Code
   |
   +--> OpenSpec CLI
   |
   +--> artefactos versionados
              |
              v
           GitHub
              |
              +--> Copilot Agent
              |
              +--> copilot/*
              |
              +--> implementación
              |
              +--> Pull Request
```

## Ejemplo práctico

Supongamos que el cambio OpenSpec ya está definido y versionado en `workshop-requirements-api`, y queremos delegar una tarea de implementación desde GitHub.

Al iniciar la tarea se selecciona:

```text
Repository: workshop-requirements-api
Base branch: <BASE_BRANCH>
Custom Agent: Developer
```

GitHub Copilot creará su propia rama de trabajo, por ejemplo:

```text
<BASE_BRANCH>
   \
    copilot/implement-validation-rule
```

Prompt de ejemplo:

```text
Trabaja únicamente en workshop-requirements-api.

Implementa la funcionalidad descrita en el GitHub Issue y en el cambio OpenSpec disponible en este repositorio.

Antes de realizar cambios:

1. Revisa las instrucciones disponibles en el repositorio.
2. Lee el GitHub Issue asociado a la tarea.
3. Identifica y lee el cambio OpenSpec relacionado: proposal, specs, design y tasks.
4. Revisa la arquitectura y los ADRs relevantes.
5. Analiza la implementación existente antes de modificar código.

Durante la implementación:

- Trabaja únicamente en este repositorio.
- Utiliza la rama proporcionada por la sesión de GitHub Copilot.
- No crees nuevas ramas.
- No ejecutes git checkout, git switch ni cambies de rama.
- Mantén la implementación alineada con proposal, specs, design y tasks aprobados.
- Implementa únicamente el alcance comprometido en el GitHub Issue.
- Agrega o actualiza las pruebas necesarias.
- No modifiques archivos que no estén relacionados con la tarea.

Al finalizar:

1. Ejecuta las validaciones y pruebas disponibles que no dependan de herramientas ausentes en el entorno del agente.
2. Comprueba que los cambios correspondan únicamente al alcance solicitado.
3. Resume los archivos modificados y las pruebas ejecutadas.
4. Indica cualquier supuesto, limitación o decisión técnica tomada.
5. Deja los cambios listos para revisión mediante Pull Request.
```

## Diferencia principal

```text
VS Code
Prompt + workspace + herramientas locales + control explícito de Git

GitHub Copilot Agent
Prompt + repositorio + rama base
                 |
                 v
            copilot/*
                 |
                 v
                PR
```

GitHub Copilot Agents es, por tanto, una **alternativa de ejecución para tareas cuyo contexto ya está suficientemente definido y versionado**, mientras que VS Code continúa siendo el flujo principal del laboratorio y el entorno natural para las operaciones explícitas de OpenSpec.
