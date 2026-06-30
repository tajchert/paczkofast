// =============================================================================
// CORE:DATASTORE MODULE
// =============================================================================
// User preferences storage using Jetpack DataStore.
//
// ## DataStore vs SharedPreferences
//
// We use DataStore instead of SharedPreferences because:
// 1. Async API - doesn't block the main thread
// 2. Type safety - typed keys prevent runtime errors
// 3. Flow-based - reactive updates to preference changes
// 4. Consistent - no partial updates or race conditions
//
// ## Implementation Choice
//
// We use Preferences DataStore (key-value) for simplicity.
// For more complex typed data, consider Proto DataStore.
// =============================================================================

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.demo.sample.core.datastore"
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

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // DataStore - using `api` so types are exposed for Hilt processing
    api(libs.datastore.preferences)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
