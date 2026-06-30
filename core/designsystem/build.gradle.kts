// =============================================================================
// CORE:DESIGNSYSTEM MODULE
// =============================================================================
// The design system module containing theme, typography, colors, and
// wrapped Material 3 components.
//
// ## Why a Separate Design System Module?
//
// 1. **Consistency**: All UI modules depend on this, ensuring consistent styling
// 2. **Single Source of Truth**: Theme changes propagate everywhere automatically
// 3. **Wrapped Components**: Custom components enforce app-specific styling
// 4. **Easier Updates**: Update Material 3 components in one place
//
// ## Usage
//
// All feature modules should depend on this module and use:
// - PaczkofastTheme {} for wrapping content
// - PaczkofastButton, PaczkofastCard, etc. instead of Material components directly
// =============================================================================

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "pl.tajchert.paczko.fast.core.designsystem"
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
    // Compose BOM - manages versions for all Compose libraries
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)

    // Compose UI
    implementation(libs.bundles.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.bundles.compose.debug)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
