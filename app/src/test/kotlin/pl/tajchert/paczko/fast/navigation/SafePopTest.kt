package pl.tajchert.paczko.fast.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Test

class SafePopTest {

    @Serializable
    private data object Root : NavKey

    @Serializable
    private data object Top : NavKey

    @Test
    fun `popEntry removes the top entry`() {
        val backStack = NavBackStack<NavKey>(Root, Top)

        backStack.popEntry()

        assertEquals(listOf<NavKey>(Root), backStack.toList())
    }

    @Test
    fun `popEntry never empties a single-entry stack`() {
        val backStack = NavBackStack<NavKey>(Root)

        backStack.popEntry()

        assertEquals(listOf<NavKey>(Root), backStack.toList())
    }

    @Test
    fun `rapid double popEntry cannot empty the stack`() {
        // Reproduces the "History tab twice fast" crash: two pops fired before
        // recomposition swaps the screen away must not leave an empty stack.
        val backStack = NavBackStack<NavKey>(Root, Top)

        backStack.popEntry()
        backStack.popEntry()

        assertEquals(listOf<NavKey>(Root), backStack.toList())
    }
}
