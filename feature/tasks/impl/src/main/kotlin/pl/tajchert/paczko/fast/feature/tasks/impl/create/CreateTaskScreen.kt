package pl.tajchert.paczko.fast.feature.tasks.impl.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastButton
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastTopAppBar
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import pl.tajchert.paczko.fast.core.model.TaskPriority

/**
 * Stateful create task screen.
 *
 * @param onBackClick Called when back navigation is requested
 * @param onTaskCreated Called after task is successfully created
 * @param viewModel The ViewModel (provided by Hilt)
 */
@Composable
fun CreateTaskScreen(
    onBackClick: () -> Unit,
    onTaskCreated: () -> Unit,
    viewModel: CreateTaskViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CreateTaskContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onPriorityChange = viewModel::onPriorityChange,
        onCreateClick = { viewModel.onCreateTask(onSuccess = onTaskCreated) },
    )
}

/**
 * Stateless create task content.
 */
@Composable
private fun CreateTaskContent(
    uiState: CreateTaskUiState,
    onBackClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriorityChange: (TaskPriority) -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PaczkofastTopAppBar(
                title = "Create Task",
                onNavigationClick = onBackClick,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Title field
            OutlinedTextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                label = { Text("Title") },
                placeholder = { Text("Enter task title") },
                isError = uiState.titleError != null,
                supportingText = uiState.titleError?.let { error ->
                    { Text(error, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Description field
            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = { Text("Description") },
                placeholder = { Text("Enter task description (optional)") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
            )

            // Priority selection
            Text(
                text = "Priority",
                style = MaterialTheme.typography.labelLarge,
            )

            PrioritySelector(
                selectedPriority = uiState.priority,
                onPrioritySelected = onPriorityChange,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Create button
            PaczkofastButton(
                text = "Create Task",
                onClick = onCreateClick,
                enabled = uiState.isValid,
                isLoading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Priority radio button group.
 *
 * ## Accessibility
 *
 * - Uses selectableGroup() for proper grouping
 * - Each radio button has contentDescription
 * - Row arrangement for horizontal layout
 */
@Composable
private fun PrioritySelector(
    selectedPriority: TaskPriority,
    onPrioritySelected: (TaskPriority) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TaskPriority.entries.forEach { priority ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.semantics {
                    contentDescription = "${priority.name} priority"
                },
            ) {
                RadioButton(
                    selected = priority == selectedPriority,
                    onClick = { onPrioritySelected(priority) },
                )
                Text(
                    text = priority.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
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
private fun CreateTaskEmptyPreview() {
    PaczkofastTheme {
        CreateTaskContent(
            uiState = CreateTaskUiState(),
            onBackClick = {},
            onTitleChange = {},
            onDescriptionChange = {},
            onPriorityChange = {},
            onCreateClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateTaskFilledPreview() {
    PaczkofastTheme {
        CreateTaskContent(
            uiState = CreateTaskUiState(
                title = "Complete documentation",
                description = "Write comprehensive docs for all the architecture patterns",
                priority = TaskPriority.HIGH,
            ),
            onBackClick = {},
            onTitleChange = {},
            onDescriptionChange = {},
            onPriorityChange = {},
            onCreateClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateTaskErrorPreview() {
    PaczkofastTheme {
        CreateTaskContent(
            uiState = CreateTaskUiState(
                title = "",
                titleError = "Title is required",
            ),
            onBackClick = {},
            onTitleChange = {},
            onDescriptionChange = {},
            onPriorityChange = {},
            onCreateClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateTaskLoadingPreview() {
    PaczkofastTheme {
        CreateTaskContent(
            uiState = CreateTaskUiState(
                title = "My Task",
                isLoading = true,
            ),
            onBackClick = {},
            onTitleChange = {},
            onDescriptionChange = {},
            onPriorityChange = {},
            onCreateClick = {},
        )
    }
}
