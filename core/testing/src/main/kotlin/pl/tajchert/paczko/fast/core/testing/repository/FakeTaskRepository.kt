package pl.tajchert.paczko.fast.core.testing.repository

import pl.tajchert.paczko.fast.core.data.repository.TaskRepository
import pl.tajchert.paczko.fast.core.model.Task
import pl.tajchert.paczko.fast.core.model.TaskPriority
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import java.util.UUID

/**
 * Fake implementation of [TaskRepository] for testing.
 *
 * ## Why Fakes?
 *
 * Fakes are preferred over mocking libraries because:
 *
 * 1. **Realistic Behavior**: This fake actually stores and retrieves tasks,
 *    so it behaves like a real repository (minus persistence).
 *
 * 2. **State Verification**: Tests can inspect state directly:
 *    ```kotlin
 *    fakeRepository.setTasks(listOf(task1, task2))
 *    viewModel.deleteTask(task1.id)
 *    assertTrue(fakeRepository.currentTasks.size == 1)
 *    ```
 *
 * 3. **No Mock Setup**: No need for `when(...).thenReturn(...)` chains.
 *
 * 4. **Compile-Time Safety**: Must implement the interface correctly.
 *
 * ## Usage
 *
 * ```kotlin
 * class TaskListViewModelTest {
 *     private val fakeRepository = FakeTaskRepository()
 *     private lateinit var viewModel: TaskListViewModel
 *
 *     @Before
 *     fun setup() {
 *         viewModel = TaskListViewModel(fakeRepository)
 *     }
 *
 *     @Test
 *     fun `shows tasks from repository`() = runTest {
 *         fakeRepository.setTasks(listOf(testTask))
 *
 *         viewModel.uiState.test {
 *             val state = awaitItem()
 *             assertEquals(1, state.tasks.size)
 *         }
 *     }
 * }
 * ```
 */
class FakeTaskRepository : TaskRepository {

    /**
     * Internal task storage.
     *
     * Using MutableSharedFlow with replay=1 so:
     * - New collectors get the latest value immediately
     * - All collectors see the same emissions
     */
    private val tasksFlow = MutableSharedFlow<List<Task>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /**
     * Current tasks (for test verification).
     */
    val currentTasks: List<Task>
        get() = tasksFlow.replayCache.firstOrNull() ?: emptyList()

    /**
     * Expose tasks as a Flow for test assertions.
     */
    val tasks: Flow<List<Task>> = tasksFlow

    /**
     * Controls whether operations should throw errors.
     * Set this to true to test error handling.
     */
    private var shouldThrowError = false

    /**
     * Set whether the repository should throw errors.
     */
    fun setShouldThrowError(value: Boolean) {
        shouldThrowError = value
    }

    /**
     * The exception to throw when [shouldThrowError] is true.
     */
    var errorToThrow: Exception = RuntimeException("Test error")

    init {
        // Initialize with empty list
        tasksFlow.tryEmit(emptyList())
    }

    // =========================================================================
    // Test Control Methods
    // =========================================================================

    /**
     * Set the tasks that the repository should return.
     *
     * This triggers an emission to all observers.
     */
    fun setTasks(tasks: List<Task>) {
        tasksFlow.tryEmit(tasks)
    }

    /**
     * Clear all tasks.
     */
    fun clear() {
        tasksFlow.tryEmit(emptyList())
    }

    // =========================================================================
    // TaskRepository Implementation
    // =========================================================================

    override fun observeTasks(): Flow<List<Task>> {
        return tasksFlow.map { tasks ->
            if (shouldThrowError) throw errorToThrow
            tasks
        }
    }

    override fun observeTask(taskId: String): Flow<Task?> {
        return tasksFlow.map { tasks -> tasks.find { it.id == taskId } }
    }

    override suspend fun createTask(
        title: String,
        description: String,
        priority: TaskPriority,
    ): Task {
        if (shouldThrowError) throw errorToThrow

        val task = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            isCompleted = false,
            createdAt = Clock.System.now(),
            priority = priority,
        )

        val current = currentTasks.toMutableList()
        current.add(task)
        tasksFlow.tryEmit(current)

        return task
    }

    override suspend fun updateTask(task: Task) {
        if (shouldThrowError) throw errorToThrow

        val current = currentTasks.toMutableList()
        val index = current.indexOfFirst { it.id == task.id }
        if (index != -1) {
            current[index] = task
            tasksFlow.tryEmit(current)
        }
    }

    override suspend fun updateTaskCompletion(taskId: String, isCompleted: Boolean) {
        if (shouldThrowError) throw errorToThrow

        val current = currentTasks.toMutableList()
        val index = current.indexOfFirst { it.id == taskId }
        if (index != -1) {
            val task = current[index]
            current[index] = task.copy(isCompleted = isCompleted)
            tasksFlow.tryEmit(current)
        }
    }

    override suspend fun deleteTask(taskId: String) {
        if (shouldThrowError) throw errorToThrow

        val current = currentTasks.filter { it.id != taskId }
        tasksFlow.tryEmit(current)
    }

    override suspend fun refreshTasks() {
        if (shouldThrowError) throw errorToThrow
        // No-op for fake - caller controls data via setTasks()
    }
}
