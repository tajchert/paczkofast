package pl.tajchert.paczko.fast.feature.tasks.impl.create

import app.cash.turbine.test
import pl.tajchert.paczko.fast.core.model.TaskPriority
import pl.tajchert.paczko.fast.core.testing.repository.FakeTaskRepository
import pl.tajchert.paczko.fast.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// =============================================================================
// CREATE TASK VIEWMODEL TESTS
// =============================================================================
// Tests for form state management and task creation.
//
// ## Form Testing Patterns
//
// Forms have different testing considerations than read-only screens:
// - Input validation must be tested
// - Error states must be checked
// - Success callbacks must be verified
// =============================================================================

class CreateTaskViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var viewModel: CreateTaskViewModel

    @Before
    fun setup() {
        taskRepository = FakeTaskRepository()
        viewModel = CreateTaskViewModel(taskRepository)
    }

    // =========================================================================
    // INITIAL STATE TESTS
    // =========================================================================

    @Test
    fun `initial state has empty fields`() = runTest {
        val state = viewModel.uiState.value

        assertEquals("", state.title)
        assertEquals("", state.description)
        assertEquals(TaskPriority.MEDIUM, state.priority)
        assertFalse(state.isLoading)
        assertNull(state.titleError)
    }

    @Test
    fun `initial state is not valid`() = runTest {
        assertFalse(viewModel.uiState.value.isValid)
    }

    // =========================================================================
    // INPUT UPDATE TESTS
    // =========================================================================

    @Test
    fun `onTitleChange updates title`() = runTest {
        viewModel.onTitleChange("New Task")

        assertEquals("New Task", viewModel.uiState.value.title)
    }

    @Test
    fun `onTitleChange with valid title makes form valid`() = runTest {
        viewModel.onTitleChange("New Task")

        assertTrue(viewModel.uiState.value.isValid)
    }

    @Test
    fun `onTitleChange with blank title shows error`() = runTest {
        // First enter a title, then clear it
        viewModel.onTitleChange("Task")
        viewModel.onTitleChange("")

        assertEquals("Title is required", viewModel.uiState.value.titleError)
        assertFalse(viewModel.uiState.value.isValid)
    }

    @Test
    fun `onDescriptionChange updates description`() = runTest {
        viewModel.onDescriptionChange("Task description")

        assertEquals("Task description", viewModel.uiState.value.description)
    }

    @Test
    fun `onPriorityChange updates priority`() = runTest {
        viewModel.onPriorityChange(TaskPriority.HIGH)

        assertEquals(TaskPriority.HIGH, viewModel.uiState.value.priority)
    }

    // =========================================================================
    // TASK CREATION TESTS
    // =========================================================================

    @Test
    fun `onCreateTask with valid form creates task`() = runTest {
        // Given: Valid form data
        viewModel.onTitleChange("Test Task")
        viewModel.onDescriptionChange("Test Description")
        viewModel.onPriorityChange(TaskPriority.HIGH)

        var successCalled = false

        // When: Create task
        viewModel.onCreateTask { successCalled = true }

        // Then: Task is created and success is called
        assertTrue(successCalled)

        // Verify task was added to repository
        taskRepository.tasks.test {
            val tasks = awaitItem()
            assertEquals(1, tasks.size)
            assertEquals("Test Task", tasks.first().title)
            assertEquals("Test Description", tasks.first().description)
            assertEquals(TaskPriority.HIGH, tasks.first().priority)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onCreateTask with blank title shows error`() = runTest {
        // Given: Invalid form (no title)
        var successCalled = false

        // When: Try to create task
        viewModel.onCreateTask { successCalled = true }

        // Then: Error is shown, success not called
        assertFalse(successCalled)
        assertEquals("Title is required", viewModel.uiState.value.titleError)
    }

    @Test
    fun `onCreateTask shows loading state`() = runTest {
        // Given: Valid form
        viewModel.onTitleChange("Test Task")

        // Use Turbine to observe state changes
        viewModel.uiState.test {
            // Initial state
            awaitItem()

            // When: Create task
            viewModel.onCreateTask { }

            // Then: Loading state is shown
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
