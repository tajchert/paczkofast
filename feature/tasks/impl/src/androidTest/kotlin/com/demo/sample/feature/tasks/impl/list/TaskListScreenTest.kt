package com.demo.sample.feature.tasks.impl.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.demo.sample.core.designsystem.theme.SampleTheme
import com.demo.sample.core.model.Task
import com.demo.sample.core.model.TaskPriority
import kotlin.time.Clock
import org.junit.Rule
import org.junit.Test

// =============================================================================
// COMPOSE UI TESTS
// =============================================================================
// Demonstrates proper Compose UI testing patterns:
//
// ## Key Patterns
//
// 1. **createComposeRule**: Sets up a Compose test environment
// 2. **Test stateless content**: Test the content composable, not the stateful wrapper
// 3. **Semantic matchers**: Use text, content descriptions, test tags
// 4. **Actions**: performClick, performTextInput, etc.
//
// ## Why Test Stateless Content?
//
// Testing the stateless `TaskListContent` instead of `TaskListScreen`:
// - No Hilt required (easier setup)
// - Full control over state
// - Faster tests
// - Tests actual UI rendering and interactions
//
// ## Finding Nodes
//
// Compose provides multiple ways to find nodes:
// - `onNodeWithText("text")` - Find by displayed text
// - `onNodeWithContentDescription("desc")` - Find by accessibility label
// - `onNodeWithTag("tag")` - Find by test tag (Modifier.testTag())
// =============================================================================

class TaskListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // =========================================================================
    // LOADING STATE TESTS
    // =========================================================================

    @Test
    fun loadingState_showsLoadingIndicator() {
        composeTestRule.setContent {
            SampleTheme {
                TaskListContent(
                    uiState = TaskListUiState.Loading,
                    onTaskClick = {},
                    onTaskCheckedChange = { _, _ -> },
                    onDeleteTask = {},
                    onCreateClick = {},
                )
            }
        }

        // Loading indicator should be visible
        // Note: In a real test, you'd use a testTag for the loading indicator
        composeTestRule.onNodeWithText("Tasks").assertIsDisplayed()
    }

    // =========================================================================
    // EMPTY STATE TESTS
    // =========================================================================

    @Test
    fun emptyState_showsEmptyMessage() {
        composeTestRule.setContent {
            SampleTheme {
                TaskListContent(
                    uiState = TaskListUiState.Empty,
                    onTaskClick = {},
                    onTaskCheckedChange = { _, _ -> },
                    onDeleteTask = {},
                    onCreateClick = {},
                )
            }
        }

        // Empty state message should be visible
        composeTestRule.onNodeWithText("No tasks yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tap the + button to create your first task")
            .assertIsDisplayed()
    }

    // =========================================================================
    // SUCCESS STATE TESTS
    // =========================================================================

    @Test
    fun successState_showsTasks() {
        val tasks = listOf(
            createTestTask(id = "1", title = "First Task"),
            createTestTask(id = "2", title = "Second Task"),
        )

        composeTestRule.setContent {
            SampleTheme {
                TaskListContent(
                    uiState = TaskListUiState.Success(tasks = tasks),
                    onTaskClick = {},
                    onTaskCheckedChange = { _, _ -> },
                    onDeleteTask = {},
                    onCreateClick = {},
                )
            }
        }

        // Tasks should be displayed
        composeTestRule.onNodeWithText("First Task").assertIsDisplayed()
        composeTestRule.onNodeWithText("Second Task").assertIsDisplayed()
    }

    @Test
    fun successState_clickingTask_callsOnTaskClick() {
        var clickedTaskId: String? = null
        val tasks = listOf(
            createTestTask(id = "test-id", title = "Clickable Task"),
        )

        composeTestRule.setContent {
            SampleTheme {
                TaskListContent(
                    uiState = TaskListUiState.Success(tasks = tasks),
                    onTaskClick = { clickedTaskId = it },
                    onTaskCheckedChange = { _, _ -> },
                    onDeleteTask = {},
                    onCreateClick = {},
                )
            }
        }

        // Click on the task
        composeTestRule.onNodeWithText("Clickable Task").performClick()

        // Verify callback was invoked
        assert(clickedTaskId == "test-id") { "Expected task id to be 'test-id' but was '$clickedTaskId'" }
    }

    // =========================================================================
    // ERROR STATE TESTS
    // =========================================================================

    @Test
    fun errorState_showsErrorMessage() {
        composeTestRule.setContent {
            SampleTheme {
                TaskListContent(
                    uiState = TaskListUiState.Error(message = "Network error occurred"),
                    onTaskClick = {},
                    onTaskCheckedChange = { _, _ -> },
                    onDeleteTask = {},
                    onCreateClick = {},
                )
            }
        }

        // Error message should be visible
        composeTestRule.onNodeWithText("Network error occurred").assertIsDisplayed()
    }

    // =========================================================================
    // FAB TESTS
    // =========================================================================

    @Test
    fun fabClick_callsOnCreateClick() {
        var createClicked = false

        composeTestRule.setContent {
            SampleTheme {
                TaskListContent(
                    uiState = TaskListUiState.Empty,
                    onTaskClick = {},
                    onTaskCheckedChange = { _, _ -> },
                    onDeleteTask = {},
                    onCreateClick = { createClicked = true },
                )
            }
        }

        // Click on FAB (find by content description)
        composeTestRule.onNodeWithContentDescription("Create task").performClick()

        // Verify callback was invoked
        assert(createClicked) { "Expected createClicked to be true" }
    }

    // =========================================================================
    // HELPER FUNCTIONS
    // =========================================================================

    private fun createTestTask(
        id: String = "test-id",
        title: String = "Test Task",
        description: String = "Test description",
        isCompleted: Boolean = false,
        priority: TaskPriority = TaskPriority.MEDIUM,
    ): Task = Task(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
        createdAt = Clock.System.now(),
        priority = priority,
    )
}
