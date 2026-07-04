package pl.tajchert.paczko.fast.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape definitions for the Paczkofast app.
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
val PaczkofastShapes = Shapes(
    // Extra small - status chips, count badges
    extraSmall = RoundedCornerShape(7.dp),

    // Small - size badges, small controls
    small = RoundedCornerShape(8.dp),

    // Medium - action buttons, QR panels
    medium = RoundedCornerShape(14.dp),

    // Large - subtle (in-transit) cards
    large = RoundedCornerShape(18.dp),

    // Extra large - prominent (ready) cards
    extraLarge = RoundedCornerShape(18.dp),
)
