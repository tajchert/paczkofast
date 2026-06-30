// =============================================================================
// FEATURE:TASKS:IMPL MODULE
// =============================================================================
// Contains the full implementation of the tasks feature:
// - Screens (Compose UI)
// - ViewModels (state management)
// - Navigation graph contribution
//
// ## Dependencies Structure
//
// This module depends on:
// - feature:tasks:api (navigation routes)
// - core:model (domain models)
// - core:data (repositories)
// - core:domain (use cases)
// - core:designsystem (theme, base components)
// - core:ui (app-specific components like TaskCard)
// - core:common (Result type, dispatchers)
//
// It does NOT depend on:
// - core:database (data layer abstraction)
// - core:network (data layer abstraction)
// - Other features (features are siblings, not parent-child)
// =============================================================================

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "pl.tajchert.paczko.fast.feature.tasks.impl"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Project modules
    implementation(projects.feature.tasks.api)
    implementation(projects.core.model)
    implementation(projects.core.data)
    implementation(projects.core.domain)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(projects.core.common)

    // Compose BOM
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.bundles.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.bundles.compose.debug)

    // Lifecycle
    implementation(libs.bundles.lifecycle)

    // Navigation 3
    implementation(libs.bundles.navigation3)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    // Date/Time
    implementation(libs.kotlinx.datetime)

    // Testing
    testImplementation(projects.core.testing)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
