package com.demo.sample.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape definitions for the Sample app.
 *
 * ## Material 3 Shape Scale
 *
 * Material 3 uses a shape scale with these sizes:
 * - Extra Small (4dp): Chips, small components
 * - Small (8dp): Buttons, text fields
 * - Medium (12dp): Cards, dialogs
 * - Large (16dp): Bottom sheets, navigation drawers
 * - Extra Large (28dp): Large dialogs, full-screen elements
 *
 * ## Usage
 *
 * Shapes are automatically applied to Material 3 components.
 * For custom shapes, access through MaterialTheme:
 *
 * ```kotlin
 * Box(
 *     modifier = Modifier
 *         .clip(MaterialTheme.shapes.medium)
 *         .background(MaterialTheme.colorScheme.surface)
 * )
 * ```
 */
val SampleShapes = Shapes(
    // Extra small - for very small components like chips
    extraSmall = RoundedCornerShape(4.dp),

    // Small - for buttons, small cards
    small = RoundedCornerShape(8.dp),

    // Medium - for cards, dialogs
    medium = RoundedCornerShape(12.dp),

    // Large - for bottom sheets, navigation drawers
    large = RoundedCornerShape(16.dp),

    // Extra large - for large dialogs, full-screen modals
    extraLarge = RoundedCornerShape(28.dp),
)
