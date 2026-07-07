// =============================================================================
// APP MODULE
// =============================================================================
// The application module that ties everything together.
//
// ## Responsibilities
//
// - Application class with Hilt setup
// - MainActivity as single entry point
// - Navigation host combining all features
// - Theme observation and application
//
// ## Dependency Approach
//
// This module depends on:
// - All feature:*:impl modules (to access navigation graphs)
// - core:designsystem (for theme)
// - core:data (for theme preferences)
// - core:model (for preference models)
//
// Features are composed here via their navigation contributions.
// =============================================================================

import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.baselineprofile)
}

// Release signing. Values come from keystore.properties (local, gitignored) or
// PACZKOFAST_* environment variables (CI). The keystore itself is private and
// never committed; see AGENTS.md public-safety rules.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun releaseSigningValue(property: String, envVar: String): String? =
    keystoreProperties.getProperty(property) ?: System.getenv(envVar)

fun requiredReleaseSigningValue(property: String, envVar: String): String =
    releaseSigningValue(property, envVar)
        ?: error("Release signing is configured, but $property/$envVar is missing.")

val releaseStorePath = releaseSigningValue("storeFile", "PACZKOFAST_KEYSTORE_PATH")

android {
    namespace = "pl.tajchert.paczko.fast"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "pl.tajchert.paczko.fast"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 4
        versionName = "0.2.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        if (releaseStorePath != null) {
            create("release") {
                storeFile = rootProject.file(releaseStorePath)
                storePassword = requiredReleaseSigningValue(
                    "storePassword",
                    "PACZKOFAST_KEYSTORE_PASSWORD",
                )
                keyAlias = requiredReleaseSigningValue("keyAlias", "PACZKOFAST_KEY_ALIAS")
                keyPassword = requiredReleaseSigningValue(
                    "keyPassword",
                    "PACZKOFAST_KEY_PASSWORD",
                )
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Falls back to the debug key when no release keystore is configured,
            // so release (and the baseline-profile plugin's derived
            // benchmarkRelease/nonMinifiedRelease variants) stays installable on
            // a device for profile recording and contributor builds.
            signingConfig = if (releaseStorePath != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }

    flavorDimensions += "mode"
    productFlavors {
        create("prod") {
            dimension = "mode"
        }
        create("demo") {
            dimension = "mode"
            applicationIdSuffix = ".demo"
            versionNameSuffix = "-demo"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Project modules
    implementation(projects.feature.auth.api)
    implementation(projects.feature.auth.impl)
    implementation(projects.feature.parcels.api)
    implementation(projects.feature.parcels.impl)
    implementation(projects.feature.settings.api)
    implementation(projects.feature.settings.impl)
    implementation(projects.core.designsystem)
    implementation(projects.core.data)
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.network)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.domain)
    "demoImplementation"(projects.core.demo)

    // Android Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)

    // Compose BOM
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.bundles.compose)
    debugImplementation(libs.bundles.compose.debug)

    // Lifecycle
    implementation(libs.bundles.lifecycle)

    // Navigation 3
    implementation(libs.bundles.navigation3)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Baseline profile: installs the bundled profile at app install time, and
    // wires the generator module that records it.
    implementation(libs.androidx.profileinstaller)
    baselineProfile(projects.baselineprofile)

    // Testing
    testImplementation(projects.core.testing)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
