package com.demo.sample.core.data.repository

import com.demo.sample.core.common.di.Dispatcher
import com.demo.sample.core.common.di.SampleDispatchers
import com.demo.sample.core.data.mapper.toEntity
import com.demo.sample.core.data.mapper.toNetworkDto
import com.demo.sample.core.database.dao.TaskDao
import com.demo.sample.core.database.entity.toEntity
import com.demo.sample.core.database.entity.toModel
import com.demo.sample.core.model.Task
import com.demo.sample.core.model.TaskPriority
import com.demo.sample.core.network.TaskNetworkDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import java.util.UUID
import javax.inject.Inject

/**
 * Default implementation of [TaskRepository] following the offline-first pattern.
 *
 * ## Offline-First Architecture
 *
 * This repository implements the offline-first pattern:
 *
 * ```
 * ┌─────────────────────────────────────────────────┐
 * │                  UI Layer                        │
 * │                     ↑                            │
 * │              Flow<List<Task>>                    │
 * │                     │                            │
 * │         ┌───────────┴───────────┐               │
 * │         │    TaskRepository     │               │
 * │         │  (this class)         │               │
 * │         └───────────┬───────────┘               │
 * │                     │                            │
 * │    ┌────────────────┴────────────────┐          │
 * │    ↓                                  ↓          │
 * │ ┌──────────┐                  ┌──────────┐      │
 * │ │   Room   │←────── sync ────→│ Network  │      │
 * │ │ Database │                  │   API    │      │
 * │ └──────────┘                  └──────────┘      │
 * └─────────────────────────────────────────────────┘
 * ```
 *
 * ## Key Principles
 *
 * 1. **Reads from Local**: All read operations query the local Room database.
 *    This ensures fast response times and offline capability.
 *
 * 2. **Writes Local First**: Write operations save to Room first, then
 *    sync to network. This prevents data loss on network failure.
 *
 * 3. **Single Source of Truth**: The local database is authoritative.
 *    Network data is used to update the local database, not displayed directly.
 *
 * ## Error Handling
 *
 * - Read errors: Let the exception propagate (Flow catches in UI)
 * - Write errors: Save locally succeeds, network sync is best-effort
 * - Sync errors: Logged but not thrown (data is still local)
 *
 * @param taskDao DAO for local database operations
 * @param networkDataSource Data source for network operations
 * @param ioDispatcher Dispatcher for IO operations
 */
internal class DefaultTaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val networkDataSource: TaskNetworkDataSource,
    @Dispatcher(SampleDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : TaskRepository {

    /**
     * Observe all tasks from the local database.
     *
     * WHY flowOn(ioDispatcher):
     * - The database query runs on the IO dispatcher
     * - The downstream (UI) collection happens on the Main dispatcher
     * - This is automatic with Room but explicit here for clarity
     */
    override fun observeTasks(): Flow<List<Task>> = taskDao.observeAllTasks()
        .map { entities -> entities.map { it.toModel() } }
        .flowOn(ioDispatcher)

    /**
     * Observe a single task from the local database.
     */
    override fun observeTask(taskId: String): Flow<Task?> = taskDao.observeTask(taskId)
        .map { entity -> entity?.toModel() }
        .flowOn(ioDispatcher)

    /**
     * Create a new task.
     *
     * ## Process
     *
     * 1. Generate UUID for the new task
     * 2. Save to local database immediately
     * 3. Attempt to sync to network (fire-and-forget)
     *
     * The task is available locally even if network sync fails.
     */
    override suspend fun createTask(
        title: String,
        description: String,
        priority: TaskPriority,
    ): Task = withContext(ioDispatcher) {
        val task = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            isCompleted = false,
            createdAt = Clock.System.now(),
            priority = priority,
        )

        // Save locally first (offline-first)
        taskDao.upsertTask(task.toEntity())

        // Then attempt network sync (best-effort)
        try {
            networkDataSource.createTask(task.toNetworkDto())
        } catch (e: Exception) {
            // Log error but don't throw - task is saved locally
            // In production, queue for retry with WorkManager
            e.printStackTrace()
        }

        task
    }

    /**
     * Update an existing task.
     */
    override suspend fun updateTask(task: Task): Unit = withContext(ioDispatcher) {
        // Save locally first
        taskDao.upsertTask(task.toEntity())

        // Then attempt network sync
        try {
            networkDataSource.updateTask(task.toNetworkDto())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Update task completion status.
     *
     * More efficient than full update - only changes one field.
     */
    override suspend fun updateTaskCompletion(
        taskId: String,
        isCompleted: Boolean,
    ) = withContext(ioDispatcher) {
        val task = taskDao.getTask(taskId) ?: return@withContext

        // Update locally
        taskDao.updateTaskCompletion(taskId, isCompleted)

        // Sync to network
        try {
            val updatedTask = task.copy(isCompleted = isCompleted).toModel()
            networkDataSource.updateTask(updatedTask.toNetworkDto())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Delete a task.
     */
    override suspend fun deleteTask(taskId: String) = withContext(ioDispatcher) {
        // Delete locally
        taskDao.deleteTaskById(taskId)

        // Sync to network
        try {
            networkDataSource.deleteTask(taskId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Refresh tasks from the network.
     *
     * Fetches all tasks from the server and updates the local database.
     * This is a full sync - existing local data is replaced.
     */
    override suspend fun refreshTasks() = withContext(ioDispatcher) {
        try {
            val networkTasks = networkDataSource.getTasks()
            val entities = networkTasks.map { it.toEntity() }
            taskDao.upsertTasks(entities)
        } catch (e: Exception) {
            // Rethrow so caller can handle (show error message)
            throw e
        }
    }
}
