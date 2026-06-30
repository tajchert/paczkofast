package com.demo.sample.feature.tasks.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.demo.sample.feature.tasks.api.CreateTaskRoute
import com.demo.sample.feature.tasks.api.TaskDetailRoute
import com.demo.sample.feature.tasks.api.TaskListRoute
import com.demo.sample.feature.tasks.impl.create.CreateTaskScreen
import com.demo.sample.feature.tasks.impl.detail.TaskDetailScreen
import com.demo.sample.feature.tasks.impl.list.TaskListScreen

// =============================================================================
// FEATURE NAVIGATION CONTRIBUTION (Navigation 3)
// =============================================================================
// This file provides an extension function that adds all task feature entries
// to the app's entry provider. This pattern allows:
//
// 1. **Encapsulation**: The app module doesn't need to know about individual
//    screens, only how to add "the tasks feature" to navigation.
//
// 2. **Single Point of Entry**: All task-related navigation is defined here.
//
// 3. **Type Safety**: Uses the NavKey routes from feature:tasks:api.
//
// ## Navigation 3 Pattern
//
// In Navigation 3 the app owns the back stack (a simple list of NavKeys).
// Features don't manipulate the back stack directly - they receive callbacks:
// - `onNavigate` pushes a new key onto the back stack
// - `onBack` pops the current key off the back stack
//
// ## Usage in App Module
//
// ```kotlin
// NavDisplay(
//     backStack = backStack,
//     entryProvider = entryProvider {
//         tasksEntries(
//             onNavigate = { backStack.add(it) },
//             onBack = { backStack.removeLastOrNull() },
//         )
//         // Other features...
//     },
// )
// ```
// =============================================================================

/**
 * Adds the tasks feature entries to the entry provider.
 *
 * This adds all task-related destinations:
 * - Task List (start destination for this feature)
 * - Task Detail
 * - Create Task
 *
 * @param onNavigate Pushes the given route onto the back stack
 * @param onBack Pops the current route off the back stack
 */
fun EntryProviderScope<NavKey>.tasksEntries(
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit,
) {
    // Task List Screen
    entry<TaskListRoute> {
        TaskListScreen(
            onTaskClick = { taskId ->
                onNavigate(TaskDetailRoute(taskId = taskId))
            },
            onCreateClick = {
                onNavigate(CreateTaskRoute)
            },
        )
    }

    // Task Detail Screen
    entry<TaskDetailRoute> { key ->
        TaskDetailScreen(
            route = key,
            onBackClick = onBack,
        )
    }

    // Create Task Screen
    entry<CreateTaskRoute> {
        CreateTaskScreen(
            onBackClick = onBack,
            onTaskCreated = {
                // Navigate back to list after creating
                onBack()
            },
        )
    }
}
