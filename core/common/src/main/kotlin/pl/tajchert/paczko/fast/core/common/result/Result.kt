package pl.tajchert.paczko.fast.core.common.result

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * A sealed interface representing the result of an operation that can be
 * in one of three states: Loading, Success, or Error.
 *
 * ## Why a Custom Result Type?
 *
 * Kotlin has a built-in `Result<T>` type, but it has limitations:
 * 1. It doesn't have a Loading state
 * 2. It's inline and has restrictions in some contexts
 * 3. We want to integrate it with Flow for reactive streams
 *
 * ## Usage with StateFlow in ViewModel
 *
 * ```kotlin
 * val uiState: StateFlow<Result<List<Task>>> = taskRepository
 *     .observeTasks()
 *     .asResult()  // Wraps in Result with Loading/Success/Error
 *     .stateIn(
 *         scope = viewModelScope,
 *         started = SharingStarted.WhileSubscribed(5_000),
 *         initialValue = Result.Loading,
 *     )
 * ```
 *
 * ## Usage in Composable
 *
 * ```kotlin
 * when (val result = uiState) {
 *     is Result.Loading -> LoadingIndicator()
 *     is Result.Success -> TaskList(result.data)
 *     is Result.Error -> ErrorMessage(result.exception)
 * }
 * ```
 *
 * @param T The type of data contained in a successful result
 */
sealed interface Result<out T> {

    /**
     * The operation is in progress.
     *
     * Typically shown when initially loading data or refreshing.
     */
    data object Loading : Result<Nothing>

    /**
     * The operation completed successfully with data.
     *
     * @property data The resulting data from the operation
     */
    data class Success<T>(val data: T) : Result<T>

    /**
     * The operation failed with an exception.
     *
     * @property exception The exception that caused the failure
     */
    data class Error(val exception: Throwable) : Result<Nothing>
}

/**
 * Converts a Flow of values to a Flow of [Result].
 *
 * This extension function:
 * 1. Emits [Result.Loading] immediately when the flow starts
 * 2. Wraps each emitted value in [Result.Success]
 * 3. Catches any exceptions and emits [Result.Error]
 *
 * ## Why Use This?
 *
 * This pattern is central to the UI state management:
 * - ViewModels expose `StateFlow<Result<T>>` instead of raw data
 * - Composables can easily handle all three states
 * - Loading and error states are handled consistently across the app
 *
 * ## Example
 *
 * ```kotlin
 * // In Repository
 * fun observeTasks(): Flow<List<Task>> = taskDao.observeAllTasks()
 *
 * // In ViewModel
 * val tasksResult: StateFlow<Result<List<Task>>> = repository
 *     .observeTasks()
 *     .asResult()
 *     .stateIn(viewModelScope, ...)
 * ```
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> = map<T, Result<T>> { Result.Success(it) }
    .onStart { emit(Result.Loading) }
    .catch { emit(Result.Error(it)) }
