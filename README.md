# Android Template

Production-ready Android project template optimized for LLM-assisted development. Based on Google's "Now in Android" reference architecture.

## Tech Stack

| Category | Technology |
|----------|------------|
| UI | Jetpack Compose, Material 3 |
| Navigation | Jetpack Navigation 3 |
| Architecture | MVVM + Clean Architecture, Multi-module |
| DI | Hilt |
| Database | Room (offline-first) |
| Preferences | Preferences DataStore |
| Network | Retrofit, OkHttp, Kotlinx Serialization |
| Async | Coroutines, Flow |
| Build | Kotlin 2.4.0, AGP 9.2.1, Version Catalog |
| SDK | Compile/Target 36, Min 34 |

## Project Structure

```
app/                    # Single-activity entry point
core/                   # Shared infrastructure
  ├── model/           # Pure Kotlin domain models
  ├── common/          # Dispatchers, Result wrapper, utilities
  ├── database/        # Room database, entities, DAOs
  ├── datastore/       # Preferences DataStore
  ├── designsystem/    # Material 3 theme, wrapped components
  ├── ui/              # App-specific UI components
  ├── network/         # Retrofit, DTOs, API abstraction
  ├── data/            # Repository implementations
  ├── domain/          # Use cases / business logic
  └── testing/         # Fakes, test rules, utilities
feature/                # Feature modules (API/Impl split)
  └── tasks/           # Sample feature
      ├── api/         # Navigation routes only
      └── impl/        # Screens, ViewModels
```

## Documentation

| Resource | Description |
|----------|-------------|
| [MODERN_ANDROID_GUIDE.md](MODERN_ANDROID_GUIDE.md) | Comprehensive architecture guide covering modules, layers, DI, Compose, navigation, data layer, testing, and more (20 chapters) |
| `feature/tasks/` | Complete sample feature demonstrating all patterns |
| Code comments | Inline explanations throughout the codebase |

## Key Patterns

- **Offline-first**: Room is the single source of truth
- **Unidirectional Data Flow**: Data up via Flow, events down via callbacks
- **State hoisting**: Separates stateful (ViewModel) from stateless (composables)
- **API/Impl split**: Features expose only navigation routes, hiding implementation
- **Fakes over mocks**: Testing uses real implementations with fake data

## Quick Start

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle
4. Run on device/emulator (API 34+)

## Adding a New Feature

1. Create `feature/<name>/api` module with navigation routes
2. Create `feature/<name>/impl` module with screens and ViewModels
3. Add navigation to `SampleNavHost` in the app module
4. Follow patterns from `feature/tasks/` as reference
