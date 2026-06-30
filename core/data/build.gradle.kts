// =============================================================================
// CORE:DATA MODULE
// =============================================================================
// Repository implementations following the offline-first pattern.
//
// ## Architecture Role
//
// This module is the single source of truth for data operations:
// - Coordinates between network, database, and datastore
// - Exposes clean domain interfaces to upper layers
// - Implements caching and sync strategies
//
// ## Key Patterns
//
// 1. **Offline-First**: Local database is the source of truth
// 2. **Repository Pattern**: Abstracts data sources from consumers
// 3. **Flow-based Reads**: Reactive data streams to UI
// 4. **Suspend-based Writes**: One-shot operations for modifications
// =============================================================================

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "pl.tajchert.paczko.fast.core.data"
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
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.network)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Date/Time
    implementation(libs.kotlinx.datetime)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
