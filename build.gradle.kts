// =============================================================================
// PACZKOFAST PROJECT - ROOT BUILD FILE
// =============================================================================
// Configuration common to all sub-projects/modules.
// Uses version catalog (gradle/libs.versions.toml) for dependency management.
// =============================================================================

plugins {
    // Android plugins - applied to modules as needed
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false

    // Kotlin plugins
    // Note: kotlin-android is NOT applied - AGP 9 ships built-in Kotlin support
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false

    // Dependency injection
    alias(libs.plugins.hilt) apply false

    // Symbol processing (for Room, Hilt)
    alias(libs.plugins.ksp) apply false

    // Data persistence
    alias(libs.plugins.room) apply false
}

// Clean task to delete build directories
tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

// Force kotlin-metadata-jvm to match Kotlin version
// This is needed because Hilt's dependencies pull in an older version
subprojects {
    configurations.all {
        resolutionStrategy {
            force("org.jetbrains.kotlin:kotlin-metadata-jvm:${libs.versions.kotlin.get()}")
        }
    }
}
