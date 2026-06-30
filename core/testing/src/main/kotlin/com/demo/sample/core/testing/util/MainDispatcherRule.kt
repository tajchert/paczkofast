package com.demo.sample.core.testing.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit rule that replaces the Main dispatcher with a test dispatcher.
 *
 * ## Why This Rule is Necessary
 *
 * ViewModels use `viewModelScope` which runs on `Dispatchers.Main`.
 * In unit tests, there's no Android Main looper, so `Dispatchers.Main`
 * throws an exception.
 *
 * This rule:
 * 1. Swaps `Dispatchers.Main` with [TestDispatcher] before each test
 * 2. Resets it back after each test
 *
 * ## UnconfinedTestDispatcher
 *
 * We use [UnconfinedTestDispatcher] by default because:
 * - It executes coroutines eagerly (no manual advancing)
 * - Makes tests simpler and more predictable
 * - Good for most ViewModel tests
 *
 * For tests that need precise control over coroutine execution,
 * pass a [StandardTestDispatcher] instead.
 *
 * ## Usage
 *
 * ```kotlin
 * class MyViewModelTest {
 *     @get:Rule
 *     val mainDispatcherRule = MainDispatcherRule()
 *
 *     @Test
 *     fun myTest() = runTest {
 *         // viewModelScope coroutines run on TestDispatcher
 *     }
 * }
 * ```
 *
 * @param testDispatcher The dispatcher to use instead of Main.
 *                       Defaults to UnconfinedTestDispatcher.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {

    /**
     * Called before each test.
     * Sets the test dispatcher as the Main dispatcher.
     */
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    /**
     * Called after each test.
     * Resets Main dispatcher to its original value.
     */
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
