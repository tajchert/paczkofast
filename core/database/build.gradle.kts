// =============================================================================
// CORE:DATABASE MODULE
// =============================================================================
// Room database implementation for local data persistence.
//
// ## Why a Separate Database Module?
//
// 1. **Isolation**: Database schema changes are contained here
// 2. **Testability**: Easy to test DAOs in isolation
// 3. **Clear Boundaries**: Only this module knows about Room internals
//
// ## Key Patterns
//
// - **Entities**: Database table representations (NOT domain models)
// - **DAOs**: Data Access Objects with Flow-based queries
// - **Mappers**: Extension functions to convert entities to domain models
// =============================================================================

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "com.demo.sample.core.database"
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

// Room schema location for migration testing
room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    // Project modules
    implementation(projects.core.model)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Room - using `api` so types are exposed for Hilt processing
    api(libs.bundles.room)
    ksp(libs.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Date/Time
    implementation(libs.kotlinx.datetime)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.room.testing)
}
