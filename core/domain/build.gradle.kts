// =============================================================================
// CORE:DOMAIN MODULE
// =============================================================================
// Business logic use cases.
//
// ## When to Use Domain Layer
//
// Use cases are OPTIONAL but recommended when:
// 1. Combining data from multiple repositories
// 2. Complex business logic needs isolation
// 3. Same logic is used by multiple ViewModels
//
// For simple CRUD operations, ViewModels can call repositories directly.
//
// ## Key Patterns
//
// - Use cases are classes with a single `invoke()` operator function
// - They can be called like functions: `getSortedTasks()`
// - They return Flow for reactive data or suspend for one-shot operations
// =============================================================================

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "pl.tajchert.paczko.fast.core.domain"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Project modules
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.data)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
