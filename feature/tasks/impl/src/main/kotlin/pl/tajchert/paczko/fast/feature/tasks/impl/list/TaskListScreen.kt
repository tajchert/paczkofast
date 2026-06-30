package pl.tajchert.paczko.fast.feature.tasks.impl.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastEmptyState
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastErrorState
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastLoadingIndicator
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastTopAppBar
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import pl.tajchert.paczko.fast.core.model.Task
import pl.tajchert.paczko.fast.core.model.TaskPriority
import pl.tajchert.paczko.fast.core.ui.TaskCard
import kotlin.time.Clock

// =============================================================================
// STATE HOISTING PATTERN
// =============================================================================
// This file demonstrates the state hoisting pattern:
//
// ## Two Composables Per Screen
//
// 1. **Stateful wrapper** (TaskListScreen):
//    - Connects to ViewModel via hiltViewModel()
//    - Collects state with collectAsStateWithLifecycle()
//    - Passes state and callbacks to stateless content
//
// 2. **Stateless content** (TaskListContent):
//    - Receives all data as parameters
//    - Has no side effects
//    - Easy to preview and test
//
// ## Why This Pattern?
//
// - **Testability**: Content can be tested without ViewModel
// - **Previews**: Content can be previewed with sample data
// - **Reusability**: Content could be used in different contexts
// - **Separation**: Business logic stays in ViewModel, UI stays in composables
// =============================================================================

/**
 * Stateful task list screen.
 *
 * This composable:
 * - Connects to the ViewModel
 * - Collects UI state lifecycle-aware
 * - Delegates rendering to [TaskListContent]
 *
 * @param onTaskClick Called when a task is clicked (navigate to detail)
 * @param onCreateClick Called when create FAB is clicked
 * @param viewModel The ViewModel (provided by Hilt)
 */
@Composable
fun TaskListScreen(
    onTaskClick: (taskId: String) -> Unit,
    onCreateClick: () -> Unit,
    viewModel: TaskListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TaskListContent(
        uiState = uiState,
        onTaskClick = onTaskClick,
        onTaskCheckedChange = viewModel::onTaskCheckedChange,
        onDeleteTask = viewModel::onDeleteTask,
        onCreateClick = onCreateClick,
    )
}

/**
 * Stateless task list content.
 *
 * This composable renders the UI based on the provided state.
 * It has no knowledge of ViewModels or data fetching.
 *
 * @param uiState The current UI state
 * @param onTaskClick Called when a task is clicked
 * @param onTaskCheckedChange Called when task checkbox is toggled
 * @param onDeleteTask Called when task delete button is clicked
 * @param onCreateClick Called when create FAB is clicked
 * @param modifier Modifier for the screen
 */
@Composable
private fun TaskListContent(
    uiState: TaskListUiState,
    onTaskClick: (taskId: String) -> Unit,
    onTaskCheckedChange: (taskId: String, isCompleted: Boolean) -> Unit,
    onDeleteTask: (taskId: String) -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PaczkofastTopAppBar(title = "Tasks")
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create task",
                )
            }
        },
    ) { paddingValues ->
        when (uiState) {
            is TaskListUiState.Loading -> {
                PaczkofastLoadingIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            }

            is TaskListUiState.Empty -> {
                PaczkofastEmptyState(
                    icon = Icons.Outlined.CheckCircle,
                    title = "No tasks yet",
                    description = "Tap the + button to create your first task",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            }

            is TaskListUiState.Success -> {
                TaskList(
                    tasks = uiState.tasks,
                    onTaskClick = onTaskClick,
                    onTaskCheckedChange = onTaskCheckedChange,
                    onDeleteTask = onDeleteTask,
                    contentPadding = paddingValues,
                )
            }

            is TaskListUiState.Error -> {
                PaczkofastErrorState(
                    message = uiState.message,
                    onRetry = { /* TODO: Add retry mechanism */ },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            }
        }
    }
}

/**
 * Task list using LazyColumn.
 *
 * ## Performance Notes
 *
 * - Uses `key` parameter for stable item identity
 * - Each item recomposes independently
 * - No state reads during scroll (TaskCard is stateless)
 */
@Composable
private fun TaskList(
    tasks: List<Task>,
    onTaskClick: (taskId: String) -> Unit,
    onTaskCheckedChange: (taskId: String, isCompleted: Boolean) -> Unit,
    onDeleteTask: (taskId: String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 88.dp, // Space for FAB
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = tasks,
            key = { task -> task.id }, // Stable key for recomposition optimization
        ) { task ->
            TaskCard(
                task = task,
                onCheckedChange = { isCompleted ->
                    onTaskCheckedChange(task.id, isCompleted)
                },
                onClick = { onTaskClick(task.id) },
                onDeleteClick = { onDeleteTask(task.id) },
            )
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================
// Multi-preview setup to test different states and configurations.
//
// ## PreviewParameterProvider
//
// We use PreviewParameterProvider to generate multiple previews from a single
// @Preview annotation. This reduces code duplication while ensuring we preview
// all important states.
// =============================================================================

private class TaskListUiStatePreviewProvider : PreviewParameterProvider<TaskListUiState> {
    override val values: Sequence<TaskListUiState> = sequenceOf(
        TaskListUiState.Loading,
        TaskListUiState.Empty,
        TaskListUiState.Success(
            tasks = listOf(
                Task(
                    id = "1",
                    title = "Complete project documentation",
                    description = "Write comprehensive docs for the architecture patterns",
                    isCompleted = false,
                    createdAt = Clock.System.now(),
                    priority = TaskPriority.HIGH,
                ),
                Task(
                    id = "2",
                    title = "Setup CI/CD pipeline",
                    description = "Configure GitHub Actions for automated builds",
                    isCompleted = true,
                    createdAt = Clock.System.now(),
                    priority = TaskPriority.MEDIUM,
                ),
                Task(
                    id = "3",
                    title = "Review pull requests",
                    description = "",
                    isCompleted = false,
                    createdAt = Clock.System.now(),
                    priority = TaskPriority.LOW,
                ),
            ),
        ),
        TaskListUiState.Error("Failed to load tasks. Please try again."),
    )
}

@Preview(showBackground = true, name = "Task List States")
@Composable
private fun TaskListContentPreview(
    @PreviewParameter(TaskListUiStatePreviewProvider::class) uiState: TaskListUiState,
) {
    PaczkofastTheme {
        TaskListContent(
            uiState = uiState,
            onTaskClick = {},
            onTaskCheckedChange = { _, _ -> },
            onDeleteTask = {},
            onCreateClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Task List - Dark Theme")
@Composable
private fun TaskListContentDarkPreview() {
    PaczkofastTheme(darkTheme = true) {
        TaskListContent(
            uiState = TaskListUiState.Success(
                tasks = listOf(
                    Task(
                        id = "1",
                        title = "Dark theme task",
                        description = "Testing dark theme appearance",
                        isCompleted = false,
                        createdAt = Clock.System.now(),
                        priority = TaskPriority.HIGH,
                    ),
                ),
            ),
            onTaskClick = {},
            onTaskCheckedChange = { _, _ -> },
            onDeleteTask = {},
            onCreateClick = {},
        )
    }
}
