package pl.tajchert.paczko.fast.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import pl.tajchert.paczko.fast.BuildConfig
import pl.tajchert.paczko.fast.core.designsystem.component.BottomNavDestination
import pl.tajchert.paczko.fast.feature.auth.api.AuthRoute
import pl.tajchert.paczko.fast.feature.auth.impl.navigation.authEntries
import pl.tajchert.paczko.fast.feature.parcels.api.ParcelListRoute
import pl.tajchert.paczko.fast.feature.parcels.impl.navigation.parcelEntries
import pl.tajchert.paczko.fast.feature.settings.api.SettingsRoute
import pl.tajchert.paczko.fast.feature.settings.impl.navigation.settingsEntries

// =============================================================================
// APP NAVIGATION DISPLAY (Navigation 3)
// =============================================================================
// Central navigation configuration for the entire app.
//
// ## Navigation 3 Concepts
//
// - **Back stack**: A snapshot-state list of NavKeys owned by the app.
//   `rememberNavBackStack` persists it across config changes & process death.
// - **NavDisplay**: Observes the back stack and renders the top entry.
// - **Entry provider**: Maps each NavKey to its composable content.
// - **Entry decorators**: Provide per-entry saved state and ViewModel scoping.
//
// ## Feature Composition
//
// Each feature contributes its entries via an extension function:
// ```kotlin
// entryProvider = entryProvider {
//     parcelEntries(...)     // Parcels feature
//     settingsEntries(...)   // Settings feature (future)
//     profileEntries(...)    // Profile feature (future)
// }
// ```
//
// ## Type-Safe Routes
//
// - Routes are @Serializable classes implementing NavKey
// - No string-based route matching
// - Arguments are type-checked at compile time
//
// ## Adding New Features
//
// To add a new feature:
// 1. Create feature:newfeature:api with NavKey routes
// 2. Create feature:newfeature:impl with screens
// 3. Add `newFeatureEntries(...)` to the entryProvider here
// =============================================================================

/**
 * Main navigation display for the app.
 *
 * @param modifier Modifier for the NavDisplay
 * @param backStack The navigation back stack (created if not provided)
 */
@Composable
fun PaczkofastNavHost(
    startDestination: NavKey = AuthRoute,
    modifier: Modifier = Modifier,
    backStack: NavBackStack<NavKey> = rememberNavBackStack(startDestination),
) {
    // Which bottom-nav tab the parcel-list shell shows. Hoisted above the back
    // stack (not kept inside ParcelListScreen) so returning from the Settings
    // route can select the requested tab instead of whatever tab was last shown.
    var parcelTab by rememberSaveable { mutableStateOf(BottomNavDestination.Parcels) }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.popEntry() },
        // The ViewModelStoreNavEntryDecorator scopes ViewModels to their
        // back stack entry, so each destination gets its own ViewModel that
        // is cleared when the entry is popped. Specifying entryDecorators
        // replaces the defaults, so the saveable-state decorator must be
        // listed explicitly as well.
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            authEntries(
                onAuthenticated = {
                    backStack.clear()
                    backStack.add(ParcelListRoute)
                },
                onOnboardingFinished = {
                    backStack.clear()
                    backStack.add(AuthRoute)
                },
            )

            parcelEntries(
                selectedTab = { parcelTab },
                onSelectTab = { parcelTab = it },
                onNavigate = { key -> backStack.add(key) },
                onBack = { backStack.popEntry() },
                onOpenSettings = { backStack.add(SettingsRoute) },
            )

            settingsEntries(
                // Settings sits on top of the parcel list; returning selects the
                // requested tab before popping back so the list shows it (rather
                // than whatever tab was last active).
                onOpenParcels = {
                    parcelTab = BottomNavDestination.Parcels
                    backStack.popEntry()
                },
                onOpenHistory = {
                    parcelTab = BottomNavDestination.History
                    backStack.popEntry()
                },
                onLoggedOut = {
                    backStack.clear()
                    backStack.add(AuthRoute)
                },
                appVersion = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            )

            // Add more features here:
            // profileEntries(...)
        },
    )
}
