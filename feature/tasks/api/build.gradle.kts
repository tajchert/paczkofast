// =============================================================================
// FEATURE:TASKS:API MODULE
// =============================================================================
// Contains ONLY the navigation routes for the tasks feature.
//
// ## API/Impl Split Pattern
//
// This pattern allows other modules to navigate TO this feature without
// depending on its implementation details:
//
// - **api module**: Navigation routes only (lightweight)
// - **impl module**: Screens, ViewModels, UI logic (heavy)
//
// Benefits:
// 1. Other features can navigate to tasks without knowing about ViewModels
// 2. Reduces build times (api changes less frequently than impl)
// 3. Clear boundary between "what" (routes) and "how" (implementation)
//
// ## Type-Safe Navigation
//
// Uses Kotlin serialization for type-safe route arguments:
// - Route classes marked with @Serializable
// - Arguments are type-checked at compile time
// - No string-based argument passing
// =============================================================================

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.demo.sample.feature.tasks.api"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Serialization for type-safe routes
    implementation(libs.kotlinx.serialization.json)

    // Navigation 3 NavKey interface for route definitions
    api(libs.androidx.navigation3.runtime)
}
