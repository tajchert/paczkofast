package pl.tajchert.paczko.fast.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import pl.tajchert.paczko.fast.core.database.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for task operations.
 *
 * ## Why Flow for Queries?
 *
 * All read queries return [Flow] instead of suspend functions because:
 *
 * 1. **Reactive Updates**: Room automatically re-emits when the tasks table changes.
 *    No need to manually refresh data after inserts/updates/deletes.
 *
 * 2. **Single Source of Truth**: The UI always reflects the current database state.
 *    Combined with StateFlow in ViewModels, this creates a reactive chain:
 *    `Database → Flow → Repository → ViewModel → Compose UI`
 *
 * 3. **Lifecycle Awareness**: When the UI stops collecting, Room stops querying.
 *
 * ## Write Operations
 *
 * Write operations (insert, update, delete) are suspend functions because:
 * - They complete once (not streams)
 * - Caller can await completion and handle errors
 *
 * ## Query Patterns
 *
 * - Use @Query for custom SQL queries
 * - Use @Upsert for insert-or-update (replaces @Insert + @Update)
 * - Use @Delete for removing entities
 */
@Dao
interface TaskDao {

    /**
     * Observe all tasks ordered by creation date (newest first).
     *
     * This is a cold Flow that:
     * - Starts querying when collected
     * - Re-emits whenever the tasks table changes
     * - Stops when collection stops
     *
     * ## Usage in Repository
     *
     * ```kotlin
     * override fun observeTasks(): Flow<List<Task>> = taskDao.observeAllTasks()
     *     .map { entities -> entities.map { it.toModel() } }
     * ```
     */
    @Query("SELECT * FROM tasks ORDER BY created_at DESC")
    fun observeAllTasks(): Flow<List<TaskEntity>>

    /**
     * Observe a single task by ID.
     *
     * Returns null if the task doesn't exist.
     * Useful for task detail screens.
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun observeTask(taskId: String): Flow<TaskEntity?>

    /**
     * Get a single task by ID (one-shot query).
     *
     * Unlike [observeTask], this doesn't observe changes.
     * Use when you need the current value without updates.
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTask(taskId: String): TaskEntity?

    /**
     * Insert or update a task.
     *
     * ## Why Upsert?
     *
     * @Upsert combines insert and update:
     * - If the task doesn't exist (by primary key), it inserts
     * - If it exists, it updates all fields
     *
     * This simplifies sync logic where we don't know if a task
     * already exists locally.
     */
    @Upsert
    suspend fun upsertTask(task: TaskEntity)

    /**
     * Insert or update multiple tasks at once.
     *
     * Used during sync operations to batch-update from network.
     */
    @Upsert
    suspend fun upsertTasks(tasks: List<TaskEntity>)

    /**
     * Delete a task.
     */
    @Delete
    suspend fun deleteTask(task: TaskEntity)

    /**
     * Delete a task by ID.
     *
     * More convenient than [deleteTask] when you only have the ID.
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    /**
     * Update only the completion status of a task.
     *
     * More efficient than updating the entire entity when
     * just toggling completion.
     */
    @Query("UPDATE tasks SET is_completed = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskCompletion(taskId: String, isCompleted: Boolean)

    /**
     * Delete all completed tasks.
     *
     * Useful for "clear completed" functionality.
     */
    @Query("DELETE FROM tasks WHERE is_completed = 1")
    suspend fun deleteCompletedTasks()

    /**
     * Count of all tasks.
     */
    @Query("SELECT COUNT(*) FROM tasks")
    fun observeTaskCount(): Flow<Int>

    /**
     * Count of completed tasks.
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 1")
    fun observeCompletedTaskCount(): Flow<Int>
}
