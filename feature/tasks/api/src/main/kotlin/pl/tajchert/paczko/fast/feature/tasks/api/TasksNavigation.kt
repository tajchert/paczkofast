package pl.tajchert.paczko.fast.feature.tasks.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// =============================================================================
// TYPE-SAFE NAVIGATION ROUTES (Navigation 3)
// =============================================================================
// These route classes define the navigation contract for the tasks feature.
//
// ## Why Type-Safe Navigation?
//
// Traditional navigation uses string-based routes with string arguments:
//   navController.navigate("task_detail/123")
//
// Problems:
// - No compile-time checking
// - Easy to typo route names
// - Arguments are stringly-typed
// - Refactoring is error-prone
//
// Navigation 3 uses plain Kotlin classes as back stack keys:
//   backStack.add(TaskDetailRoute(taskId = "123"))
//
// Benefits:
// - Compile-time type checking
// - IDE autocomplete
// - Safe refactoring
// - Arguments have proper types
//
// ## NavKey + @Serializable
//
// Each route implements [NavKey] and is marked @Serializable so the back
// stack created with `rememberNavBackStack` survives configuration changes
// and process death.
//
// ## Usage in NavDisplay
//
// ```kotlin
// val backStack = rememberNavBackStack(TaskListRoute)
// NavDisplay(
//     backStack = backStack,
//     entryProvider = entryProvider {
//         entry<TaskListRoute> { TaskListScreen(...) }
//         entry<TaskDetailRoute> { key -> TaskDetailScreen(taskId = key.taskId) }
//     },
// )
// ```
// =============================================================================

/**
 * Route to the task list screen.
 *
 * This is a singleton route (object) because it takes no parameters.
 */
@Serializable
data object TaskListRoute : NavKey

/**
 * Route to the task detail screen.
 *
 * @param taskId The ID of the task to display.
 */
@Serializable
data class TaskDetailRoute(
    val taskId: String,
) : NavKey

/**
 * Route to the create task screen.
 *
 * This is a singleton route because creating a new task needs no parameters.
 * If you wanted to support editing, you could add an optional taskId:
 *
 * ```kotlin
 * @Serializable
 * data class CreateEditTaskRoute(val taskId: String? = null) : NavKey
 * ```
 */
@Serializable
data object CreateTaskRoute : NavKey
