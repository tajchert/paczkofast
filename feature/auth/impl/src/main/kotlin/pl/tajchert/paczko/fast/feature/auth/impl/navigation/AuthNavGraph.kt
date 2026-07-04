package pl.tajchert.paczko.fast.feature.auth.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import pl.tajchert.paczko.fast.feature.auth.api.AuthRoute
import pl.tajchert.paczko.fast.feature.auth.api.OnboardingRoute
import pl.tajchert.paczko.fast.feature.auth.impl.AuthScreen
import pl.tajchert.paczko.fast.feature.auth.impl.OnboardingScreen

fun EntryProviderScope<NavKey>.authEntries(
    onAuthenticated: () -> Unit,
    onOnboardingFinished: () -> Unit,
) {
    entry<OnboardingRoute> {
        OnboardingScreen(onFinished = onOnboardingFinished)
    }

    entry<AuthRoute> {
        AuthScreen(onAuthenticated = onAuthenticated)
    }
}
