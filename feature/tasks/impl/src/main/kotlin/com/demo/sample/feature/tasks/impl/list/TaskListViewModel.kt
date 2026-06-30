package com.demo.sample.feature.tasks.impl.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.sample.core.common.result.Result
import com.demo.sample.core.common.result.asResult
import com.demo.sample.core.data.repository.TaskRepository
import com.demo.sample.core.domain.GetSortedTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// =============================================================================
// VIEWMODEL PATTERN
// =============================================================================
// This ViewModel demonstrates the recommended patterns from Now in Android:
//
// ## StateFlow with stateIn
//
// We use `stateIn` to convert Flow to StateFlow because:
// 1. StateFlow replays the last value to new collectors
// 2. We can specify an initial value for immediate UI rendering
// 3. WhileSubscribed(5_000) keeps the flow alive briefly during config changes
//
// ## Why WhileSubscribed(5_000)?
//
// - The "5000" is 5 seconds in milliseconds
// - During rotation, the old collector stops and new one starts
// - 5 second buffer prevents restarting the upstream flow
// - Saves resources when user leaves the screen
//
// ## Result Wrapper
//
// We wrap the Flow in Result to handle:
// - Loading state (initial)
// - Success state (with data)
// - Error state (with exception)
//
// This eliminates try-catch in the ViewModel and moves error handling
// to the flow transformation.
// =============================================================================

@HiltViewModel
class TaskListViewModel @Inject constructor(
    getSortedTasksUseCase: GetSortedTasksUseCase,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    /**
     * UI state exposed to the screen.
     *
     * This is computed from the sorted tasks use case, wrapped in Result,
     * and transformed to UI state.
     *
     * ## Flow Pipeline
     *
     * ```
     * getSortedTasksUseCase()  // Flow<List<Task>>
     *   → asResult()           // Flow<Result<List<Task>>>
     *   → map { ... }          // Flow<TaskListUiState>
     *   → stateIn(...)         // StateFlow<TaskListUiState>
     * ```
     */
    val uiState: StateFlow<TaskListUiState> = getSortedTasksUseCase()
        .asResult()
        .map { result ->
            when (result) {
                is Result.Loading -> TaskListUiState.Loading
                is Result.Success -> {
                    if (result.data.isEmpty()) {
                        TaskListUiState.Empty
                    } else {
                        TaskListUiState.Success(tasks = result.data)
                    }
                }
                is Result.Error -> TaskListUiState.Error(
                    message = result.exception.message ?: "Unknown error occurred",
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TaskListUiState.Loading,
        )

    // =========================================================================
    // USER ACTIONS
    // =========================================================================
    // These methods handle user interactions from the UI.
    // They launch coroutines in viewModelScope for fire-and-forget operations.
    //
    // Note: We don't expose loading states for these actions because:
    // 1. They're typically fast (local DB operations)
    // 2. The main uiState will update when the data changes
    // 3. For slow operations, consider adding action-specific loading states
    // =========================================================================

    /**
     * Toggle task completion status.
     *
     * @param taskId The ID of the task to toggle
     * @param isCompleted The new completion status
     */
    fun onTaskCheckedChange(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.updateTaskCompletion(taskId, isCompleted)
        }
    }

    /**
     * Delete a task.
     *
     * @param taskId The ID of the task to delete
     */
    fun onDeleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
        }
    }
}
