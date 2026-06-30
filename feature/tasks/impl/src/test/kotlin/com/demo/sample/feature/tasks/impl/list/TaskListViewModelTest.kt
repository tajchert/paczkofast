package com.demo.sample.feature.tasks.impl.list

import app.cash.turbine.test
import com.demo.sample.core.data.repository.TaskRepository
import com.demo.sample.core.data.repository.UserPreferencesRepository
import com.demo.sample.core.domain.GetSortedTasksUseCase
import com.demo.sample.core.model.TaskPriority
import com.demo.sample.core.testing.repository.FakeTaskRepository
import com.demo.sample.core.testing.repository.FakeUserPreferencesRepository
import com.demo.sample.core.testing.testTask
import com.demo.sample.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// =============================================================================
// VIEWMODEL UNIT TESTS
// =============================================================================
// Demonstrates proper ViewModel testing patterns:
//
// ## Key Patterns
//
// 1. **MainDispatcherRule**: Replaces Main dispatcher for testing
// 2. **Turbine**: Tests StateFlow emissions over time
// 3. **Fake Repositories**: Simple, predictable test doubles
// 4. **No Mocking**: Fakes are preferred over Mockito/Mockk
//
// ## Test Structure
//
// - @Before: Create fresh fakes and ViewModel
// - Each test: Set up fakes → trigger action → assert state
//
// ## Turbine Usage
//
// Turbine provides `test {}` block for testing Flows:
// - `awaitItem()`: Get next emission
// - `cancelAndIgnoreRemainingEvents()`: Clean up
//
// ## StateFlow Conflation Gotcha
//
// With MainDispatcherRule's UnconfinedTestDispatcher, the `stateIn` sharing
// coroutine runs eagerly and synchronously the moment Turbine subscribes.
// By the time the first item is delivered, the intermediate Loading value
// has already been conflated away - the first `awaitItem()` returns the
// settled state (Success/Empty/Error), NOT Loading. Don't write
// `awaitItem() // skip Loading` - it will eat the state you wanted and the
// next `awaitItem()` will time out.
// =============================================================================

class TaskListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var userPreferencesRepository: FakeUserPreferencesRepository
    private lateinit var getSortedTasksUseCase: GetSortedTasksUseCase
    private lateinit var viewModel: TaskListViewModel

    @Before
    fun setup() {
        taskRepository = FakeTaskRepository()
        userPreferencesRepository = FakeUserPreferencesRepository()
        getSortedTasksUseCase = GetSortedTasksUseCase(
            taskRepository = taskRepository,
            userPreferencesRepository = userPreferencesRepository,
        )
    }

    private fun createViewModel(): TaskListViewModel {
        return TaskListViewModel(
            getSortedTasksUseCase = getSortedTasksUseCase,
            taskRepository = taskRepository,
        )
    }

    // =========================================================================
    // INITIAL STATE TESTS
    // =========================================================================

    @Test
    fun `initial state is Loading`() = runTest {
        viewModel = createViewModel()

        assertEquals(TaskListUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `state is Empty when no tasks exist`() = runTest {
        // Given: Empty repository
        taskRepository.setTasks(emptyList())

        // When: ViewModel is created
        viewModel = createViewModel()

        // Then: State settles to Empty
        // (Loading is conflated away - see "StateFlow Conflation Gotcha" above)
        viewModel.uiState.test {
            assertEquals(TaskListUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state is Success when tasks exist`() = runTest {
        // Given: Repository with tasks
        val tasks = listOf(
            testTask(id = "1", title = "Task 1"),
            testTask(id = "2", title = "Task 2"),
        )
        taskRepository.setTasks(tasks)

        // When: ViewModel is created
        viewModel = createViewModel()

        // Then: State settles to Success with tasks
        viewModel.uiState.test {
            val successState = awaitItem()
            assertTrue(successState is TaskListUiState.Success)
            assertEquals(2, (successState as TaskListUiState.Success).tasks.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // =========================================================================
    // ACTION TESTS
    // =========================================================================

    @Test
    fun `onTaskCheckedChange updates task completion`() = runTest {
        // Given: Repository with incomplete task
        val task = testTask(id = "1", isCompleted = false)
        taskRepository.setTasks(listOf(task))
        viewModel = createViewModel()

        // Wait for initial state
        viewModel.uiState.test {
            awaitItem() // Settled Success state

            // When: Toggle completion
            viewModel.onTaskCheckedChange("1", true)

            // Then: Task is updated
            val updatedState = awaitItem() as TaskListUiState.Success
            assertTrue(updatedState.tasks.first().isCompleted)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDeleteTask removes task from list`() = runTest {
        // Given: Repository with tasks
        val tasks = listOf(
            testTask(id = "1", title = "Task 1"),
            testTask(id = "2", title = "Task 2"),
        )
        taskRepository.setTasks(tasks)
        viewModel = createViewModel()

        // Wait for initial state
        viewModel.uiState.test {
            awaitItem() // Settled Success state with 2 tasks

            // When: Delete first task
            viewModel.onDeleteTask("1")

            // Then: Only one task remains
            val updatedState = awaitItem() as TaskListUiState.Success
            assertEquals(1, updatedState.tasks.size)
            assertEquals("2", updatedState.tasks.first().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleting last task results in Empty state`() = runTest {
        // Given: Repository with single task
        val task = testTask(id = "1")
        taskRepository.setTasks(listOf(task))
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Settled Success state

            // When: Delete the only task
            viewModel.onDeleteTask("1")

            // Then: State becomes Empty
            assertEquals(TaskListUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // =========================================================================
    // ERROR HANDLING TESTS
    // =========================================================================

    @Test
    fun `state is Error when repository throws exception`() = runTest {
        // Given: Repository that will throw
        taskRepository.setShouldThrowError(true)

        // When: ViewModel is created
        viewModel = createViewModel()

        // Then: State settles to Error
        viewModel.uiState.test {
            val errorState = awaitItem()
            assertTrue(errorState is TaskListUiState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
