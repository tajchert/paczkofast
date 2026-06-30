package pl.tajchert.paczko.fast.feature.tasks.impl.list

import pl.tajchert.paczko.fast.core.model.Task

// =============================================================================
// SEALED UI STATE
// =============================================================================
// Using sealed interface (not sealed class) for UI state because:
// 1. More concise - no need for empty bodies
// 2. Better Kotlin idiom
// 3. Works perfectly with `when` exhaustive checking
//
// ## State Design Principles
//
// 1. **Mutually exclusive states**: Only one state can be active at a time
// 2. **Complete information**: Each state contains everything needed to render UI
// 3. **No derived state**: Don't compute UI state in composables
//
// ## Why separate Empty from Success?
//
// Some teams use Success(emptyList()) for empty state. We use a separate
// Empty state because:
// - UI treatment is often different (empty state illustration vs. list)
// - Intent is clearer in code
// - Easier to add empty-specific properties (e.g., firstTimeUser)
// =============================================================================

/**
 * UI state for the task list screen.
 */
sealed interface TaskListUiState {

    /**
     * Initial loading state.
     *
     * Shown when the screen first loads and we're fetching data.
     */
    data object Loading : TaskListUiState

    /**
     * No tasks available.
     *
     * Shown when the database has no tasks. The UI should display
     * an encouraging message to create the first task.
     */
    data object Empty : TaskListUiState

    /**
     * Tasks loaded successfully.
     *
     * @param tasks List of tasks to display (never empty for this state)
     */
    data class Success(
        val tasks: List<Task>,
    ) : TaskListUiState

    /**
     * Error loading tasks.
     *
     * @param message User-friendly error message
     */
    data class Error(
        val message: String,
    ) : TaskListUiState
}
