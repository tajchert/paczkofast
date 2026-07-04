package pl.tajchert.paczko.fast.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * Pops the top entry off the back stack, but never the last remaining one.
 *
 * [androidx.navigation3.ui.NavDisplay] requires a non-empty back stack and
 * throws `IllegalArgumentException: NavDisplay backstack cannot be empty` if it
 * ever observes an empty one. A plain `removeLastOrNull()` wired to a back or
 * tab control can trigger that: two taps landing before the first pop
 * recomposes (a rapid double-tap) fire the handler twice, and the second pop
 * empties a stack that was one entry from the root. Guarding on [size] makes the
 * pop safe to over-invoke.
 */
internal fun NavBackStack<NavKey>.popEntry() {
    if (size > 1) removeAt(lastIndex)
}
