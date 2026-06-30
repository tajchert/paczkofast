package com.demo.sample.core.testing

import com.demo.sample.core.model.Task
import com.demo.sample.core.model.TaskPriority
import kotlin.time.Clock
import kotlin.time.Instant
import java.util.UUID

/**
 * Test data builders for unit tests.
 *
 * ## Why Test Data Builders?
 *
 * Builders help create test data by:
 * 1. Providing sensible defaults
 * 2. Allowing customization of specific fields
 * 3. Reducing boilerplate in tests
 *
 * ## Usage
 *
 * ```kotlin
 * // Use defaults
 * val task = testTask()
 *
 * // Customize specific fields
 * val completedTask = testTask(isCompleted = true)
 * val highPriorityTask = testTask(priority = TaskPriority.HIGH)
 * ```
 */

/**
 * Creates a test Task with sensible defaults.
 *
 * All parameters are optional - override only what you need.
 */
fun testTask(
    id: String = UUID.randomUUID().toString(),
    title: String = "Test Task",
    description: String = "Test task description",
    isCompleted: Boolean = false,
    createdAt: Instant = Clock.System.now(),
    priority: TaskPriority = TaskPriority.MEDIUM,
): Task = Task(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    createdAt = createdAt,
    priority = priority,
)

/**
 * Creates a list of test tasks with varying properties.
 *
 * Useful when testing list displays or filtering.
 */
fun testTaskList(
    count: Int = 5,
    completed: Int = 2,
): List<Task> {
    val priorities = TaskPriority.entries
    return (0 until count).map { index ->
        testTask(
            id = "task-$index",
            title = "Task ${index + 1}",
            description = "Description for task ${index + 1}",
            isCompleted = index < completed,
            priority = priorities[index % priorities.size],
        )
    }
}
