// =============================================================================
// CORE:MODEL MODULE
// =============================================================================
// Pure Kotlin module containing domain models.
//
// WHY a separate model module:
// 1. No Android dependencies - these are pure Kotlin data classes
// 2. Can be shared across all other modules
// 3. Enables potential Kotlin Multiplatform in the future
// 4. Faster compilation (no Android overhead)
// =============================================================================

plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // kotlinx-datetime for cross-platform date/time handling
    // WHY: java.time works but kotlinx-datetime is better for potential KMP migration
    implementation(libs.kotlinx.datetime)
}
