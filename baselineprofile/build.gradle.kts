// =============================================================================
// BASELINE PROFILE GENERATOR MODULE
// =============================================================================
// A `com.android.test` module that drives the release app on a device to record
// a Baseline Profile (AOT-compiled hot paths) for faster cold startup.
//
// Generate with:
//   ./gradlew :app:generateBaselineProfile
// The result is written to app/src/release/generated/baselineProfiles/ and
// bundled into the app; androidx.profileinstaller applies it at install time.
// =============================================================================

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "pl.tajchert.paczko.fast.baselineprofile"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        // Baseline profile generation requires API 28+.
        minSdk = 28
        targetSdk = libs.versions.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        missingDimensionStrategy("mode", "prod")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // The app module this profile is generated for.
    targetProjectPath = ":app"
}

// Generate against a physical/connected device by default.
baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.espresso.core)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}
