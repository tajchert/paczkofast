package pl.tajchert.paczko.fast.feature.settings.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.tajchert.paczko.fast.core.data.repository.AuthRepository
import pl.tajchert.paczko.fast.core.data.repository.UserPreferencesRepository
import pl.tajchert.paczko.fast.core.model.LockerOpenMode
import pl.tajchert.paczko.fast.core.model.ParcelListOpenButtonMode
import pl.tajchert.paczko.fast.core.model.ThemeMode

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferencesRepository.userPreferences,
        authRepository.observePhoneNumber(),
    ) { preferences, phoneNumber ->
        SettingsUiState(
            themeMode = preferences.themeMode,
            phoneNumber = phoneNumber,
            lockerOpenMode = preferences.lockerOpenMode,
            parcelListOpenButtonMode = preferences.parcelListOpenButtonMode,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SettingsUiState(),
        )

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(themeMode)
        }
    }

    fun setLockerOpenMode(mode: LockerOpenMode) {
        viewModelScope.launch {
            userPreferencesRepository.setLockerOpenMode(mode)
        }
    }

    fun setParcelListOpenButtonMode(mode: ParcelListOpenButtonMode) {
        viewModelScope.launch {
            userPreferencesRepository.setParcelListOpenButtonMode(mode)
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onDone()
        }
    }
}

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val phoneNumber: String? = null,
    val lockerOpenMode: LockerOpenMode = LockerOpenMode.HOLD,
    val parcelListOpenButtonMode: ParcelListOpenButtonMode = ParcelListOpenButtonMode.FIRST,
)
