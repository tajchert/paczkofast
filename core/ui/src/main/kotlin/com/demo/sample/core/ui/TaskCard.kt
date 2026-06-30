package com.demo.sample.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.demo.sample.core.designsystem.component.SampleCard
import com.demo.sample.core.designsystem.theme.SampleTheme
import com.demo.sample.core.model.Task
import com.demo.sample.core.model.TaskPriority
import kotlin.time.Clock

/**
 * Card component for displaying a task in a list.
 *
 * ## Design Decisions
 *
 * 1. **Checkbox on left**: Quick toggle without entering detail view
 * 2. **Strikethrough for completed**: Clear visual feedback
 * 3. **Priority badge**: Quick priority identification
 * 4. **Delete button**: Swipe-to-delete alternative for accessibility
 *
 * ## State Hoisting
 *
 * This component is stateless - all state is hoisted to the caller:
 * - `onCheckedChange` for completion toggle
 * - `onClick` for navigation to detail
 * - `onDeleteClick` for deletion
 *
 * @param task The task to display
 * @param onCheckedChange Called when checkbox is toggled
 * @param onClick Called when the card is clicked
 * @param onDeleteClick Called when delete button is clicked
 * @param modifier Modifier for the card
 */
@Composable
fun TaskCard(
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SampleCard(
        onClick = onClick,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Checkbox for completion
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onCheckedChange,
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Task content
            Column(
                modifier = Modifier.weight(1f),
            ) {
                // Title with optional strikethrough
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isCompleted) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                    color = if (task.isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // Description (if present)
                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Priority badge
                PriorityBadge(priority = task.priority)
            }

            // Delete button
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete task",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true)
@Composable
private fun TaskCardPreview() {
    SampleTheme {
        TaskCard(
            task = Task(
                id = "1",
                title = "Complete project documentation",
                description = "Write comprehensive docs for the architecture patterns used in this sample project.",
                isCompleted = false,
                createdAt = Clock.System.now(),
                priority = TaskPriority.HIGH,
            ),
            onCheckedChange = {},
            onClick = {},
            onDeleteClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskCardCompletedPreview() {
    SampleTheme {
        TaskCard(
            task = Task(
                id = "2",
                title = "Setup Room database",
                description = "Completed task example",
                isCompleted = true,
                createdAt = Clock.System.now(),
                priority = TaskPriority.MEDIUM,
            ),
            onCheckedChange = {},
            onClick = {},
            onDeleteClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
