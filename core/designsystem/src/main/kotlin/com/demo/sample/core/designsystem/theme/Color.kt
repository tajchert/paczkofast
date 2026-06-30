package com.demo.sample.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * Color palette for the Sample app design system.
 *
 * ## Color Naming Convention
 *
 * Colors are named with format: `{ColorFamily}{Shade}`
 * - ColorFamily: Blue, Green, Orange, etc.
 * - Shade: 10 (lightest) to 90 (darkest) in steps of 10
 *
 * This follows Material 3 conventions and makes it easy to
 * create balanced light/dark color schemes.
 *
 * ## Usage
 *
 * These colors should NOT be used directly in composables.
 * Instead, use the semantic colors from MaterialTheme.colorScheme:
 *
 * ```kotlin
 * // DON'T do this:
 * Text(color = Blue40)
 *
 * // DO this:
 * Text(color = MaterialTheme.colorScheme.primary)
 * ```
 */

// Primary - Blue palette
internal val Blue10 = Color(0xFF001F2A)
internal val Blue20 = Color(0xFF003547)
internal val Blue30 = Color(0xFF004D67)
internal val Blue40 = Color(0xFF006688)
internal val Blue80 = Color(0xFF86D1FF)
internal val Blue90 = Color(0xFFC5E7FF)

// Secondary - Teal palette
internal val Teal10 = Color(0xFF001F26)
internal val Teal20 = Color(0xFF003640)
internal val Teal30 = Color(0xFF004E5C)
internal val Teal40 = Color(0xFF006879)
internal val Teal80 = Color(0xFF4DD9F0)
internal val Teal90 = Color(0xFFACEDFF)

// Tertiary - Orange palette
internal val Orange10 = Color(0xFF2D1600)
internal val Orange20 = Color(0xFF4A2800)
internal val Orange30 = Color(0xFF693C00)
internal val Orange40 = Color(0xFF8A5100)
internal val Orange80 = Color(0xFFFFB86C)
internal val Orange90 = Color(0xFFFFDDB8)

// Error - Red palette
internal val Red10 = Color(0xFF410001)
internal val Red20 = Color(0xFF680003)
internal val Red30 = Color(0xFF930006)
internal val Red40 = Color(0xFFBA1B1B)
internal val Red80 = Color(0xFFFFB4A9)
internal val Red90 = Color(0xFFFFDAD4)

// Neutral - Gray palette for backgrounds and surfaces
internal val Gray10 = Color(0xFF1A1C1E)
internal val Gray20 = Color(0xFF2F3033)
internal val Gray90 = Color(0xFFE2E2E5)
internal val Gray95 = Color(0xFFF0F0F3)
internal val Gray99 = Color(0xFFFCFCFF)

// Neutral Variant - For containers and outlines
internal val GrayVariant30 = Color(0xFF42474E)
internal val GrayVariant50 = Color(0xFF72777F)
internal val GrayVariant60 = Color(0xFF8C9198)
internal val GrayVariant80 = Color(0xFFC2C7CF)
internal val GrayVariant90 = Color(0xFFDEE3EB)

// Priority colors for tasks
val PriorityHigh = Color(0xFFE53935)
val PriorityMedium = Color(0xFFFFA726)
val PriorityLow = Color(0xFF66BB6A)
