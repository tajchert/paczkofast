package com.demo.sample.feature.tasks.impl.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.sample.core.data.repository.TaskRepository
import com.demo.sample.core.model.TaskPriority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// =============================================================================
// FORM STATE MANAGEMENT
// =============================================================================
// This ViewModel demonstrates form state management patterns.
//
// ## MutableStateFlow for Form State
//
// Unlike read-only data that comes from repositories, form state is:
// - Modified directly by user input
// - Not persisted until explicitly saved
// - Needs immediate UI updates on each keystroke
//
// For this, we use MutableStateFlow with direct updates, not a repository pattern.
//
// ## Single State Object
//
// We combine all form fields into a single [CreateTaskUiState] object rather
// than having separate StateFlows for each field. This:
// - Reduces the number of collectors
// - Makes state easier to reason about
// - Simplifies validation that depends on multiple fields
// =============================================================================

/**
 * UI state for the create task screen.
 */
data class CreateTaskUiState(
    val title: String = "",
    val description: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isLoading: Boolean = false,
    val titleError: String? = null,
) {
    /**
     * Whether the form is valid and can be submitted.
     */
    val isValid: Boolean = title.isNotBlank() && titleError == null
}

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTaskUiState())
    val uiState: StateFlow<CreateTaskUiState> = _uiState.asStateFlow()

    // =========================================================================
    // FORM FIELD UPDATES
    // =========================================================================
    // Each field update is a separate function for clarity.
    // We use `update {}` for thread-safe state modifications.
    // =========================================================================

    /**
     * Update the task title.
     */
    fun onTitleChange(title: String) {
        _uiState.update { currentState ->
            currentState.copy(
                title = title,
                titleError = if (title.isBlank()) "Title is required" else null,
            )
        }
    }

    /**
     * Update the task description.
     */
    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    /**
     * Update the task priority.
     */
    fun onPriorityChange(priority: TaskPriority) {
        _uiState.update { it.copy(priority = priority) }
    }

    // =========================================================================
    // FORM SUBMISSION
    // =========================================================================

    /**
     * Create the task.
     *
     * @param onSuccess Called after the task is successfully created
     */
    fun onCreateTask(onSuccess: () -> Unit) {
        val currentState = _uiState.value

        // Validate before submitting
        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                taskRepository.createTask(
                    title = currentState.title,
                    description = currentState.description,
                    priority = currentState.priority,
                )
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        titleError = "Failed to create task: ${e.message}",
                    )
                }
            }
        }
    }
}
