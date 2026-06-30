// =============================================================================
// CORE:TESTING MODULE
// =============================================================================
// Test utilities, fakes, and rules for unit testing.
//
// ## Purpose
//
// This module provides:
// 1. Fake implementations of repositories (not mocks!)
// 2. Test rules for coroutines (MainDispatcherRule)
// 3. Test data builders
// 4. Common test utilities
//
// ## Why Fakes Over Mocks?
//
// We prefer fakes (test doubles that implement real interfaces) over
// mocking libraries (Mockito, MockK) because:
//
// 1. **More Realistic**: Fakes behave like real implementations
// 2. **Easier to Debug**: You can step through fake code
// 3. **No Magic**: No mock framework DSL to learn
// 4. **Reusable**: One fake works for all tests
// 5. **Compile-Time Safety**: Fakes must implement the interface correctly
// =============================================================================

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "pl.tajchert.paczko.fast.core.testing"
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
    implementation(projects.core.data)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    api(libs.kotlinx.coroutines.test)

    // Date/Time
    implementation(libs.kotlinx.datetime)

    // Testing (exposed as API for test modules)
    api(libs.junit)
    api(libs.turbine)
}
