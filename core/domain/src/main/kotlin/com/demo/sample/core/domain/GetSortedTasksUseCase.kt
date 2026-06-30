package com.demo.sample.core.domain

import com.demo.sample.core.data.repository.TaskRepository
import com.demo.sample.core.data.repository.UserPreferencesRepository
import com.demo.sample.core.model.Task
import com.demo.sample.core.model.TaskSortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Use case that combines tasks with user preferences to return sorted tasks.
 *
 * ## Why a Use Case?
 *
 * This use case exists because:
 * 1. **Combines Multiple Sources**: Tasks from [TaskRepository] + sort order from
 *    [UserPreferencesRepository]
 * 2. **Reusable**: Could be used by multiple ViewModels (list, search, widgets)
 * 3. **Testable**: Business logic is isolated and easy to unit test
 *
 * ## Alternative: Composite Repository
 *
 * For simpler combinations, you could create a CompositeTaskRepository that
 * combines these internally. Use cases are better when:
 * - The combination involves business logic (like sorting)
 * - The same combination is needed in different contexts
 *
 * ## Operator Invoke Pattern
 *
 * Use cases implement `operator fun invoke()` so they can be called like functions:
 *
 * ```kotlin
 * val getSortedTasks = GetSortedTasksUseCase(taskRepo, prefsRepo)
 * val tasksFlow = getSortedTasks()  // Calls invoke()
 * ```
 *
 * This makes the calling code cleaner and more readable.
 */
class GetSortedTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    /**
     * Returns a Flow of tasks sorted according to user preferences.
     *
     * ## How combine() Works
     *
     * `combine` creates a new Flow that:
     * 1. Waits for both source Flows to emit at least once
     * 2. Re-emits whenever EITHER source emits
     * 3. Uses the latest value from each source
     *
     * So when tasks change OR sort preference changes, a new sorted list is emitted.
     *
     * @return Flow of sorted tasks that updates reactively
     */
    operator fun invoke(): Flow<List<Task>> = combine(
        taskRepository.observeTasks(),
        userPreferencesRepository.userPreferences,
    ) { tasks, preferences ->
        sortTasks(tasks, preferences.sortOrder)
    }

    /**
     * Sort tasks according to the specified order.
     *
     * ## Sorting Logic
     *
     * - CREATED_DATE: Newest first (descending by createdAt)
     * - PRIORITY: Highest first (descending by priority ordinal)
     * - ALPHABETICAL: A-Z by title (case-insensitive)
     *
     * Within each sort, completed tasks are shown after incomplete tasks.
     */
    private fun sortTasks(tasks: List<Task>, sortOrder: TaskSortOrder): List<Task> {
        // First, separate incomplete and completed tasks
        val (incomplete, completed) = tasks.partition { !it.isCompleted }

        // Sort each group according to the sort order
        val sortedIncomplete = when (sortOrder) {
            TaskSortOrder.CREATED_DATE -> incomplete.sortedByDescending { it.createdAt }
            TaskSortOrder.PRIORITY -> incomplete.sortedByDescending { it.priority.ordinal }
            TaskSortOrder.ALPHABETICAL -> incomplete.sortedBy { it.title.lowercase() }
        }

        val sortedCompleted = when (sortOrder) {
            TaskSortOrder.CREATED_DATE -> completed.sortedByDescending { it.createdAt }
            TaskSortOrder.PRIORITY -> completed.sortedByDescending { it.priority.ordinal }
            TaskSortOrder.ALPHABETICAL -> completed.sortedBy { it.title.lowercase() }
        }

        // Return incomplete tasks first, then completed
        return sortedIncomplete + sortedCompleted
    }
}
