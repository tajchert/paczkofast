package pl.tajchert.paczko.fast.feature.settings.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import pl.tajchert.paczko.fast.feature.settings.api.SettingsRoute
import pl.tajchert.paczko.fast.feature.settings.impl.SettingsScreen

fun EntryProviderScope<NavKey>.settingsEntries(
    onOpenParcels: () -> Unit,
    onOpenHistory: () -> Unit,
    onLoggedOut: () -> Unit,
    appVersion: String,
) {
    entry<SettingsRoute> {
        SettingsScreen(
            appVersion = appVersion,
            onOpenParcels = onOpenParcels,
            onOpenHistory = onOpenHistory,
            onLoggedOut = onLoggedOut,
        )
    }
}
