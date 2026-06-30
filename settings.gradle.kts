// =============================================================================
// PACZKOFAST PROJECT - SETTINGS
// =============================================================================
// Module configuration for multi-module architecture following Now in Android patterns.
// =============================================================================

pluginManagement {
    // Include build-logic for convention plugins (optional, for advanced setups)
    // includeBuild("build-logic")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Paczkofast"

// =============================================================================
// MODULES
// =============================================================================

// Application module - entry point
include(":app")

// -----------------------------------------------------------------------------
// Core modules - shared infrastructure used across features
// -----------------------------------------------------------------------------

// Pure Kotlin domain models (no Android dependencies)
include(":core:model")

// Shared utilities, extensions, dispatcher qualifiers
include(":core:common")

// Material 3 theme, custom wrapped components
include(":core:designsystem")

// Shared UI components that use the design system
include(":core:ui")

// Room database, entities, DAOs
include(":core:database")

// Preferences DataStore for user preferences
include(":core:datastore")

// Retrofit services, network DTOs
include(":core:network")

// Repository implementations (offline-first pattern)
include(":core:data")

// Use cases / business logic
include(":core:domain")

// Test utilities, fakes, rules for unit testing
include(":core:testing")

// -----------------------------------------------------------------------------
// Feature modules - user-facing features with API/impl split
// -----------------------------------------------------------------------------

// Tasks feature - public navigation routes
include(":feature:tasks:api")

// Tasks feature - screens, ViewModels, implementation
include(":feature:tasks:impl")

// =============================================================================
// Type-safe project accessors (e.g., projects.core.model)
// =============================================================================
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
