// =============================================================================
// CORE:UI MODULE
// =============================================================================
// Shared UI components that are specific to this app's domain.
//
// ## vs core:designsystem
//
// - **core:designsystem**: Generic Material 3 components (Button, Card, etc.)
// - **core:ui**: App-specific components (TaskCard, PriorityBadge, etc.)
//
// Components in this module:
// - Know about domain models (Task, TaskPriority)
// - Are reused across multiple features
// - Build on top of designsystem components
// =============================================================================

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.demo.sample.core.ui"
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
    implementation(projects.core.model)
    implementation(projects.core.designsystem)

    // Compose BOM
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.bundles.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.bundles.compose.debug)

    // Date/Time
    implementation(libs.kotlinx.datetime)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
