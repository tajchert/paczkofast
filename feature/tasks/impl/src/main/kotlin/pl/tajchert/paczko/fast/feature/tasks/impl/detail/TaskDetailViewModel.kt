package pl.tajchert.paczko.fast.feature.tasks.impl.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pl.tajchert.paczko.fast.core.common.result.Result
import pl.tajchert.paczko.fast.core.common.result.asResult
import pl.tajchert.paczko.fast.core.data.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// =============================================================================
// ASSISTED INJECTION FOR NAVIGATION ARGUMENTS (Navigation 3)
// =============================================================================
// This ViewModel demonstrates how to receive type-safe navigation arguments.
//
// ## Why Assisted Injection?
//
// In Navigation 3 the app owns the back stack, and navigation arguments are
// plain properties of the NavKey - they are no longer delivered through
// SavedStateHandle. The recommended pattern is Hilt assisted injection:
//
// ```kotlin
// viewModel = hiltViewModel<TaskDetailViewModel, TaskDetailViewModel.Factory>(
//     creationCallback = { factory -> factory.create(route.taskId) },
// )
// ```
//
// This provides compile-time safety for navigation arguments - no parsing,
// no string keys, just a constructor parameter.
// =============================================================================

@HiltViewModel(assistedFactory = TaskDetailViewModel.Factory::class)
class TaskDetailViewModel @AssistedInject constructor(
    @Assisted private val taskId: String,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    /**
     * Factory for creating the ViewModel with its navigation argument.
     */
    @AssistedFactory
    interface Factory {
        fun create(taskId: String): TaskDetailViewModel
    }

    /**
     * UI state for the task detail screen.
     */
    val uiState: StateFlow<TaskDetailUiState> = taskRepository
        .observeTask(taskId)
        .asResult()
        .map { result ->
            when (result) {
                is Result.Loading -> TaskDetailUiState.Loading
                is Result.Success -> {
                    val task = result.data
                    if (task != null) {
                        TaskDetailUiState.Success(task = task)
                    } else {
                        TaskDetailUiState.NotFound
                    }
                }
                is Result.Error -> TaskDetailUiState.Error(
                    message = result.exception.message ?: "Unknown error occurred",
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TaskDetailUiState.Loading,
        )

    // =========================================================================
    // USER ACTIONS
    // =========================================================================

    /**
     * Toggle task completion status.
     */
    fun onToggleCompletion() {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is TaskDetailUiState.Success) {
                val task = currentState.task
                taskRepository.updateTaskCompletion(task.id, !task.isCompleted)
            }
        }
    }

    /**
     * Delete the task.
     *
     * @param onDeleted Called after the task is successfully deleted
     */
    fun onDelete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
            onDeleted()
        }
    }
}
