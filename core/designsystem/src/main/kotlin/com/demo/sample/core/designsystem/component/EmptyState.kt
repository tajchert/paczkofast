package com.demo.sample.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.demo.sample.core.designsystem.theme.SampleTheme

/**
 * Empty state component shown when there's no content to display.
 *
 * ## When to Use
 *
 * Show this component when:
 * - A list is empty (no tasks, no search results)
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
 * if (tasks.isEmpty()) {
 *     SampleEmptyState(
 *         icon = Icons.Outlined.CheckCircle,
 *         title = "No tasks yet",
 *         description = "Create your first task to get started",
 *         actionLabel = "Create Task",
 *         onAction = { navigateToCreateTask() },
 *     )
 * }
 * ```
 *
 * @param icon Icon to display above the title
 * @param title Main message explaining the empty state
 * @param modifier Modifier for the container
 * @param description Optional additional description
 * @param actionLabel Optional label for the action button
 * @param onAction Optional callback for the action button
 */
@Composable
fun SampleEmptyState(
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
        Icon(
            imageVector = icon,
            contentDescription = null, // Decorative
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )

        if (description != null) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))

            SampleButton(
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
fun SampleErrorState(
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
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (onRetry != null) {
            Spacer(modifier = Modifier.height(24.dp))

            SampleOutlinedButton(
                onClick = onRetry,
                text = "Try Again",
            )
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true)
@Composable
private fun SampleEmptyStatePreview() {
    SampleTheme {
        SampleEmptyState(
            icon = Icons.Outlined.Inbox,
            title = "No tasks yet",
            description = "Create your first task to get started",
            actionLabel = "Create Task",
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SampleEmptyStateMinimalPreview() {
    SampleTheme {
        SampleEmptyState(
            icon = Icons.Outlined.Inbox,
            title = "No results found",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SampleErrorStatePreview() {
    SampleTheme {
        SampleErrorState(
            message = "Unable to load tasks. Please check your connection.",
            onRetry = {},
        )
    }
}
