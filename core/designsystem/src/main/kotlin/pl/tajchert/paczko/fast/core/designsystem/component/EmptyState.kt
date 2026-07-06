package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Empty state component shown when there's no content to display.
 *
 * ## When to Use
 *
 * Show this component when:
 * - A list is empty (no parcels, no search results)
 * - Content hasn't been created yet
 * - A filter returns no results
 *
 * ## Design Guidelines
 *
 * - Always include a message explaining why the state is empty
 * - Include an action button when the user can do something about it
 * - Use a relevant icon to make the state quickly recognizable
 *
 * ## Usage
 *
 * ```kotlin
 * if (parcels.isEmpty()) {
 *     PaczkofastEmptyState(
 *         icon = Icons.Outlined.Inbox,
 *         title = "No parcels yet",
 *         description = "Add a shipment to start tracking delivery updates",
 *         actionLabel = "Add Parcel",
 *         onAction = { navigateToAddParcel() },
 *     )
 * }
 * ```
 *
 * @param icon Icon to display above the title, inside a neo-brutalist tile
 * @param title Main message explaining the empty state
 * @param modifier Modifier for the container
 * @param description Optional additional description
 * @param actionLabel Optional label for the action button
 * @param onAction Optional callback for the action button
 */
@Composable
fun PaczkofastEmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        EmptyStateTile(icon = icon, tint = PaczkofastTheme.colors.onAccent)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = PaczkofastTheme.colors.textPrimary,
        )

        if (description != null) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MonoLabel,
                textAlign = TextAlign.Center,
                color = PaczkofastTheme.colors.monoLabel,
            )
        }

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))

            PaczkofastButton(
                onClick = onAction,
                text = actionLabel,
            )
        }
    }
}

/**
 * Error state component for when something goes wrong.
 *
 * @param message Error message to display
 * @param modifier Modifier for the container
 * @param onRetry Optional retry action
 */
@Composable
fun PaczkofastErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        EmptyStateTile(icon = Icons.Outlined.ErrorOutline, tint = PaczkofastTheme.colors.alertText)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = PaczkofastTheme.colors.textPrimary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MonoLabel,
            textAlign = TextAlign.Center,
            color = PaczkofastTheme.colors.monoLabel,
        )

        if (onRetry != null) {
            Spacer(modifier = Modifier.height(24.dp))

            PaczkofastOutlinedButton(
                onClick = onRetry,
                text = "Try Again",
            )
        }
    }
}

/** Shared ink-bordered yellow icon tile used above empty/error captions. */
@Composable
private fun EmptyStateTile(icon: ImageVector, tint: Color) {
    NeoSurface(
        modifier = Modifier.size(84.dp),
        shape = RoundedCornerShape(20.dp),
        fill = PaczkofastTheme.colors.accent,
        borderColor = PaczkofastTheme.colors.accentBorder,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // Decorative
            modifier = Modifier
                .align(Alignment.Center)
                .size(36.dp),
            tint = tint,
        )
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true)
@Composable
private fun PaczkofastEmptyStatePreview() {
    PaczkofastTheme {
        PaczkofastEmptyState(
            icon = Icons.Outlined.Inbox,
            title = "No parcels yet",
            description = "Add a shipment to start tracking delivery updates",
            actionLabel = "Add Parcel",
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PaczkofastEmptyStateMinimalPreview() {
    PaczkofastTheme {
        PaczkofastEmptyState(
            icon = Icons.Outlined.Inbox,
            title = "No results found",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PaczkofastErrorStatePreview() {
    PaczkofastTheme {
        PaczkofastErrorState(
            message = "Unable to load parcels. Please check your connection.",
            onRetry = {},
        )
    }
}
