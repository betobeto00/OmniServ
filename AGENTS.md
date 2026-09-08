# AGENTS.md - OmniServ

## Nivel 0: Contexto Global

OmniServ es una app Android para gestión de servicios múltiples.
Parte del ecosistema OmniMargen.

**Repositorios relacionados:**
- `tog-admin`: POS desktop (Electron + React + TypeScript)
- `tog-platform`: Backend de licencias (Node.js + SQLite)
- `landing-page`: Sitio de marketing (Next.js)
- `omniserv`: Esta app (Android + Kotlin)

## Convenciones

- **Idioma**: Responder en español. Commits en inglés (imperativo).
- **Arquitectura**: MVVM + Clean Architecture, 3 capas (data/domain/ui).
- **Stack**: Kotlin, Jetpack Compose, Room, Hilt, Material 3.
- **Testing**: Verificar con `./gradlew assembleDebug` antes de commits.
- **Secrets**: Nunca commitear API keys, keystore passwords, ni licencias.
- **Nombres**: PascalCase para clases, camelCase para funciones/variables.
- **Strings**: No hardcodear strings en composables. Usar resources o constantes.
- **Colores**: Definir en Theme.kt, no hardcoded en composables.

## Estructura del Proyecto

```
app/src/main/java/com/omnimargen/omniserv/
├── data/           # Room entities, DAOs, repositories, mappers
├── domain/         # Models, use cases
├── ui/             # Screens, viewmodels, theme, navigation
├── license/        # License validation, generation
├── notification/   # WorkManager, notifications
├── update/         # Auto-update system
└── util/           # Helpers (Date, PDF)
```

## Comandos de Verificación

```bash
./gradlew assembleDebug      # Compilar debug
./gradlew assembleRelease    # Compilar release
./gradlew lint               # Lint check
```

## Licenciamiento

Ver `LICENSE_SYSTEM.md` para detalles completos.
La licencia se valida contra TOG Platform.
Private key nunca en el cliente.
