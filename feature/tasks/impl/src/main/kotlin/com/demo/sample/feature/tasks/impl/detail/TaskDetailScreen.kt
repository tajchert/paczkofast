package com.demo.sample.feature.tasks.impl.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.demo.sample.core.designsystem.component.SampleErrorState
import com.demo.sample.core.designsystem.component.SampleLoadingIndicator
import com.demo.sample.core.designsystem.component.SampleTopAppBar
import com.demo.sample.core.designsystem.theme.SampleTheme
import com.demo.sample.core.model.Task
import com.demo.sample.core.model.TaskPriority
import com.demo.sample.feature.tasks.api.TaskDetailRoute
import com.demo.sample.core.ui.PriorityBadge
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Clock

/**
 * Stateful task detail screen.
 *
 * The route's [taskId][com.demo.sample.feature.tasks.api.TaskDetailRoute.taskId]
 * is passed to the ViewModel via Hilt assisted injection. The
 * ViewModelStoreNavEntryDecorator (installed in SampleNavHost) scopes each
 * ViewModel to its back stack entry, so every TaskDetailRoute instance gets
 * its own ViewModel.
 *
 * @param route The route containing the task ID to display
 * @param onBackClick Called when back navigation is requested
 * @param viewModel The ViewModel (provided by Hilt)
 */
@Composable
fun TaskDetailScreen(
    route: TaskDetailRoute,
    onBackClick: () -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel<TaskDetailViewModel, TaskDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(route.taskId) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TaskDetailContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onToggleCompletion = viewModel::onToggleCompletion,
        onDelete = { viewModel.onDelete(onDeleted = onBackClick) },
    )
}

/**
 * Stateless task detail content.
 */
@Composable
private fun TaskDetailContent(
    uiState: TaskDetailUiState,
    onBackClick: () -> Unit,
    onToggleCompletion: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            SampleTopAppBar(
                title = "Task Details",
                onNavigationClick = onBackClick,
            )
        },
    ) { paddingValues ->
        when (uiState) {
            is TaskDetailUiState.Loading -> {
                SampleLoadingIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            }

            is TaskDetailUiState.NotFound -> {
                SampleErrorState(
                    message = "Task not found",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            }

            is TaskDetailUiState.Success -> {
                TaskDetailBody(
                    task = uiState.task,
                    onToggleCompletion = onToggleCompletion,
                    onDelete = onDelete,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                )
            }

            is TaskDetailUiState.Error -> {
                SampleErrorState(
                    message = uiState.message,
                    onRetry = { /* TODO: Add retry */ },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            }
        }
    }
}

@Composable
private fun TaskDetailBody(
    task: Task,
    onToggleCompletion: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // Title with checkbox
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleCompletion() },
            )
            Text(
                text = task.title,
                style = MaterialTheme.typography.headlineSmall,
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
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Priority badge
        PriorityBadge(priority = task.priority)

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        if (task.description.isNotBlank()) {
            Text(
                text = "Description",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Created date
        Text(
            text = "Created",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        val javaInstant = java.time.Instant.ofEpochMilli(
            (task.createdAt - kotlin.time.Instant.fromEpochMilliseconds(0)).inWholeMilliseconds
        )
        val localDateTime = javaInstant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd 'at' HH:mm")
        Text(
            text = localDateTime.format(formatter),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.weight(1f))

        // Delete button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Delete Task")
            }
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true)
@Composable
private fun TaskDetailSuccessPreview() {
    SampleTheme {
        TaskDetailContent(
            uiState = TaskDetailUiState.Success(
                task = Task(
                    id = "1",
                    title = "Complete project documentation",
                    description = "Write comprehensive docs for the architecture patterns used in this sample project. Include diagrams and code examples.",
                    isCompleted = false,
                    createdAt = Clock.System.now(),
                    priority = TaskPriority.HIGH,
                ),
            ),
            onBackClick = {},
            onToggleCompletion = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskDetailCompletedPreview() {
    SampleTheme {
        TaskDetailContent(
            uiState = TaskDetailUiState.Success(
                task = Task(
                    id = "2",
                    title = "Setup Room database",
                    description = "Already completed",
                    isCompleted = true,
                    createdAt = Clock.System.now(),
                    priority = TaskPriority.MEDIUM,
                ),
            ),
            onBackClick = {},
            onToggleCompletion = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskDetailLoadingPreview() {
    SampleTheme {
        TaskDetailContent(
            uiState = TaskDetailUiState.Loading,
            onBackClick = {},
            onToggleCompletion = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskDetailNotFoundPreview() {
    SampleTheme {
        TaskDetailContent(
            uiState = TaskDetailUiState.NotFound,
            onBackClick = {},
            onToggleCompletion = {},
            onDelete = {},
        )
    }
}
