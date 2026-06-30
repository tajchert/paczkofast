package pl.tajchert.paczko.fast.core.network

import pl.tajchert.paczko.fast.core.network.dto.NetworkTask

/**
 * Abstraction for task-related network operations.
 *
 * ## Why an Interface?
 *
 * Using an interface allows us to:
 *
 * 1. **Swap Implementations**: Demo flavor uses [FakeTaskNetworkDataSource]
 *    that reads from local JSON. Prod flavor uses real Retrofit API.
 *
 * 2. **Testing**: Tests can use a fake that returns controlled responses.
 *
 * 3. **Decoupling**: Repositories depend on this interface, not Retrofit.
 *    If we switch to Ktor, only the implementation changes.
 *
 * ## Error Handling
 *
 * Implementations should throw exceptions for network errors.
 * The repository layer catches these and handles them appropriately.
 *
 * ## Usage
 *
 * ```kotlin
 * class TaskRepository @Inject constructor(
 *     private val networkDataSource: TaskNetworkDataSource,
 *     private val taskDao: TaskDao,
 * ) {
 *     suspend fun refreshTasks() {
 *         val networkTasks = networkDataSource.getTasks()
 *         taskDao.upsertTasks(networkTasks.map { it.toEntity() })
 *     }
 * }
 * ```
 */
interface TaskNetworkDataSource {

    /**
     * Fetch all tasks from the server.
     *
     * @return List of tasks from the API
     * @throws IOException if network request fails
     */
    suspend fun getTasks(): List<NetworkTask>

    /**
     * Fetch a single task by ID.
     *
     * @param id The task ID to fetch
     * @return The task if found
     * @throws IOException if network request fails
     * @throws HttpException if task not found (404)
     */
    suspend fun getTask(id: String): NetworkTask

    /**
     * Create a new task.
     *
     * @param task The task to create
     * @return The created task with server-assigned fields
     */
    suspend fun createTask(task: NetworkTask): NetworkTask

    /**
     * Update an existing task.
     *
     * @param task The task to update (ID must exist)
     * @return The updated task
     */
    suspend fun updateTask(task: NetworkTask): NetworkTask

    /**
     * Delete a task.
     *
     * @param id The ID of the task to delete
     */
    suspend fun deleteTask(id: String)
}
