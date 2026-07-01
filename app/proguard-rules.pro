# =============================================================================
# APP-SPECIFIC PROGUARD / R8 RULES
# =============================================================================
# Most libraries used in this app (Compose, Hilt, Room, Retrofit,
# kotlinx-serialization, Coil) ship their own consumer rules, so this file
# only needs rules specific to your app code.
#
# Add rules here when you:
# - Reflectively access classes (keep them explicitly)
# - Serialize models with kotlinx-serialization in this module
# - See R8 warnings/errors during release builds
#
# Debugging tips:
# - Keep source file names and line numbers for readable crash reports:
-keepattributes SourceFile,LineNumberTable
# - Hide the original source file name:
#-renamesourcefileattribute SourceFile
