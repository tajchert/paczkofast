package com.demo.sample.core.network.fake

import com.demo.sample.core.common.di.Dispatcher
import com.demo.sample.core.common.di.SampleDispatchers
import com.demo.sample.core.network.TaskNetworkDataSource
import com.demo.sample.core.network.dto.NetworkTask
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import java.util.UUID
import javax.inject.Inject

/**
 * Fake implementation of [TaskNetworkDataSource] for development and testing.
 *
 * ## Purpose
 *
 * This fake implementation:
 * 1. **Works Offline**: No network connection needed during development
 * 2. **Predictable Data**: Returns consistent sample data for demos
 * 3. **Simulates Delays**: Adds realistic network latency for UI testing
 * 4. **In-Memory Storage**: Changes persist within the app session
 *
 * ## Usage
 *
 * In the demo build flavor, Hilt binds this instead of the real Retrofit
 * implementation:
 *
 * ```kotlin
 * @Module
 * @InstallIn(SingletonComponent::class)
 * abstract class NetworkModule {
 *     @Binds
 *     abstract fun bindsTaskNetworkDataSource(
 *         impl: FakeTaskNetworkDataSource
 *     ): TaskNetworkDataSource
 * }
 * ```
 */
class FakeTaskNetworkDataSource @Inject constructor(
    @Dispatcher(SampleDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : TaskNetworkDataSource {

    /**
     * In-memory task storage.
     * Initialized with sample data for demos.
     */
    private val tasks = mutableListOf(
        NetworkTask(
            id = "1",
            title = "Set up project architecture",
            description = "Create multi-module structure with core and feature modules following Now in Android patterns.",
            isCompleted = true,
            createdAt = "2024-01-15T09:00:00Z",
            priority = "HIGH",
        ),
        NetworkTask(
            id = "2",
            title = "Implement Room database",
            description = "Create TaskEntity, TaskDao, and TaskDatabase with proper migrations setup.",
            isCompleted = true,
            createdAt = "2024-01-15T10:30:00Z",
            priority = "HIGH",
        ),
        NetworkTask(
            id = "3",
            title = "Add Compose UI components",
            description = "Build design system with SampleTheme, SampleButton, SampleCard and other wrapped Material 3 components.",
            isCompleted = false,
            createdAt = "2024-01-15T14:00:00Z",
            priority = "MEDIUM",
        ),
        NetworkTask(
            id = "4",
            title = "Write unit tests",
            description = "Add ViewModel tests using MainDispatcherRule and fake repositories.",
            isCompleted = false,
            createdAt = "2024-01-16T09:00:00Z",
            priority = "MEDIUM",
        ),
        NetworkTask(
            id = "5",
            title = "Review documentation",
            description = "Ensure all public APIs have KDoc comments explaining the 'why' behind decisions.",
            isCompleted = false,
            createdAt = "2024-01-16T11:00:00Z",
            priority = "LOW",
        ),
    )

    override suspend fun getTasks(): List<NetworkTask> = withContext(ioDispatcher) {
        // Simulate network delay
        delay(SIMULATED_DELAY_MS)
        tasks.toList()
    }

    override suspend fun getTask(id: String): NetworkTask = withContext(ioDispatcher) {
        delay(SIMULATED_DELAY_MS)
        tasks.find { it.id == id }
            ?: throw NoSuchElementException("Task not found: $id")
    }

    override suspend fun createTask(task: NetworkTask): NetworkTask = withContext(ioDispatcher) {
        delay(SIMULATED_DELAY_MS)

        // Generate ID if not provided (simulating server behavior)
        val newTask = task.copy(
            id = task.id.ifEmpty { UUID.randomUUID().toString() },
            createdAt = task.createdAt.ifEmpty { Clock.System.now().toString() },
        )
        tasks.add(newTask)
        newTask
    }

    override suspend fun updateTask(task: NetworkTask): NetworkTask = withContext(ioDispatcher) {
        delay(SIMULATED_DELAY_MS)

        val index = tasks.indexOfFirst { it.id == task.id }
        if (index == -1) {
            throw NoSuchElementException("Task not found: ${task.id}")
        }
        tasks[index] = task
        task
    }

    override suspend fun deleteTask(id: String): Unit = withContext(ioDispatcher) {
        delay(SIMULATED_DELAY_MS)
        tasks.removeAll { it.id == id }
    }

    companion object {
        /**
         * Simulated network delay in milliseconds.
         * Helps test loading states and ensures UI handles async properly.
         */
        private const val SIMULATED_DELAY_MS = 500L
    }
}
