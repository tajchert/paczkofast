package pl.tajchert.paczko.fast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pl.tajchert.paczko.fast.core.data.repository.UserPreferencesRepository
import pl.tajchert.paczko.fast.core.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// =============================================================================
// MAIN ACTIVITY VIEWMODEL
// =============================================================================
// This ViewModel provides data needed by MainActivity BEFORE individual screens
// are shown. Specifically:
//
// - Theme configuration (dark/light/system)
// - Whether to show splash screen (while loading preferences)
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
     * Preferences loaded - show content with proper theme.
     */
    data class Success(
        val preferences: UserPreferences,
    ) : MainActivityUiState
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    /**
     * UI state for the main activity.
     *
     * Starts as Loading, then emits Success once preferences are available.
     */
    val uiState: StateFlow<MainActivityUiState> = userPreferencesRepository
        .userPreferences
        .map { MainActivityUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
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
fun MainActivityUiState.shouldUseDarkTheme(isSystemInDarkTheme: Boolean): Boolean {
    return when (this) {
        is MainActivityUiState.Loading -> isSystemInDarkTheme
        is MainActivityUiState.Success -> preferences.darkTheme
    }
}
