package pl.tajchert.paczko.fast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import pl.tajchert.paczko.fast.core.data.repository.UserPreferencesRepository
import pl.tajchert.paczko.fast.core.domain.ObserveAuthSessionUseCase
import pl.tajchert.paczko.fast.core.model.ThemeMode
import pl.tajchert.paczko.fast.core.model.UserPreferences
import pl.tajchert.paczko.fast.feature.auth.api.AuthRoute
import pl.tajchert.paczko.fast.feature.auth.api.OnboardingRoute
import pl.tajchert.paczko.fast.feature.parcels.api.ParcelListRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// =============================================================================
// MAIN ACTIVITY VIEWMODEL
// =============================================================================
// This ViewModel provides data needed by MainActivity BEFORE individual screens
// are shown. Specifically:
//
// - Theme configuration (dark/light/system)
// - Initial route based on stored authentication
// - Whether to show splash screen while startup state loads
//
// ## Why a Separate ViewModel?
//
// MainActivity needs to know the theme before rendering any UI. If we waited
// for screen ViewModels, we'd see a flash of wrong theme. This ViewModel loads
// immediately when the Activity starts.
// =============================================================================

/**
 * UI state for MainActivity.
 */
sealed interface MainActivityUiState {
    /**
     * Loading preferences - show splash screen.
     */
    data object Loading : MainActivityUiState

    /**
     * Startup state loaded - show content with proper theme and initial route.
     */
    data class Success(
        val preferences: UserPreferences,
        val initialRoute: NavKey,
    ) : MainActivityUiState
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
    observeAuthSession: ObserveAuthSessionUseCase,
) : ViewModel() {

    /**
     * UI state for the main activity.
     *
     * Starts as Loading, then emits Success once preferences and auth state are available.
     */
    val uiState: StateFlow<MainActivityUiState> = combine(
        userPreferencesRepository.userPreferences,
        observeAuthSession(),
    ) { preferences, authSession ->
        MainActivityUiState.Success(
            preferences = preferences,
            initialRoute = when {
                !preferences.hasSeenOnboarding -> OnboardingRoute
                authSession.isAuthenticated -> ParcelListRoute
                else -> AuthRoute
            },
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = MainActivityUiState.Loading,
        )
}

/**
 * Determine if dark theme should be used based on preferences.
 *
 * This is an extension function for easy use in composables:
 * ```kotlin
 * val darkTheme = uiState.shouldUseDarkTheme()
 * ```
 */
fun MainActivityUiState.shouldUseDarkTheme(@Suppress("unused") isSystemInDarkTheme: Boolean): Boolean {
    // Dark mode is temporarily disabled while the dark palette is unfinished.
    // Force light everywhere, even for a persisted DARK preference or System on a
    // dark device. To re-enable, restore the themeMode/isSystemInDarkTheme mapping
    // and re-add the Dark option in SettingsScreen.
    return false
}
