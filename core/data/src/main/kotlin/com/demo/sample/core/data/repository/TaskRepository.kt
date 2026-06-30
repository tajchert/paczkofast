package com.demo.sample.core.data.repository

import com.demo.sample.core.model.Task
import com.demo.sample.core.model.TaskPriority
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for task operations.
 *
 * ## Architecture Role
 *
 * The repository is the single access point for task data:
 * - ViewModels and Use Cases depend on this interface
 * - Implementation details (Room, Retrofit) are hidden
 * - Enables easy testing with fake implementations
 *
 * ## Offline-First Pattern
 *
 * All read operations return data from the local database:
 * - Immediate access without network latency
 * - Works offline
 * - Consistent data across the app
 *
 * Write operations update local first, then sync to network.
 *
 * ## Flow vs Suspend
 *
 * - **Flow**: For data that changes over time (task list, single task)
 * - **Suspend**: For one-shot operations (create, update, delete)
 */
interface TaskRepository {

    /**
     * Observe all tasks.
     *
     * Returns a Flow that:
     * - Emits immediately with current local data
     * - Re-emits whenever tasks change (local or synced)
     * - Never completes (hot flow)
     *
     * ## Usage in ViewModel
     *
     * ```kotlin
     * val tasks: StateFlow<List<Task>> = taskRepository.observeTasks()
     *     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
     * ```
     */
    fun observeTasks(): Flow<List<Task>>

    /**
     * Observe a single task by ID.
     *
     * @param taskId The ID of the task to observe
     * @return Flow emitting the task, or null if not found
     */
    fun observeTask(taskId: String): Flow<Task?>

    /**
     * Create a new task.
     *
     * The task is:
     * 1. Saved to local database immediately
     * 2. Synced to network in background
     *
     * @param title Task title
     * @param description Task description
     * @param priority Task priority
     * @return The created task with generated ID
     */
    suspend fun createTask(
        title: String,
        description: String,
        priority: TaskPriority,
    ): Task

    /**
     * Update an existing task.
     *
     * @param task The task to update (must have valid ID)
     */
    suspend fun updateTask(task: Task)

    /**
     * Update the completion status of a task.
     *
     * @param taskId The ID of the task to update
     * @param isCompleted The new completion status
     */
    suspend fun updateTaskCompletion(taskId: String, isCompleted: Boolean)

    /**
     * Delete a task.
     *
     * @param taskId The ID of the task to delete
     */
    suspend fun deleteTask(taskId: String)

    /**
     * Refresh tasks from network.
     *
     * Fetches latest data from the server and updates local database.
     * Call this on pull-to-refresh or app launch.
     */
    suspend fun refreshTasks()
}
