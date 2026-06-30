package pl.tajchert.paczko.fast.feature.auth.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import pl.tajchert.paczko.fast.feature.auth.api.AuthRoute
import pl.tajchert.paczko.fast.feature.auth.impl.AuthScreen

fun EntryProviderScope<NavKey>.authEntries(
    onAuthenticated: () -> Unit,
) {
    entry<AuthRoute> {
        AuthScreen(onAuthenticated = onAuthenticated)
    }
}
