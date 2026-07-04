package pl.tajchert.paczko.fast.feature.auth.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import pl.tajchert.paczko.fast.core.data.repository.UserPreferencesRepository

/**
 * Backs [OnboardingScreen]. Its only job is to persist that the user has
 * completed the first-launch welcome + disclaimer flow, so it is never
 * shown to them again (see `UserPreferences.hasSeenOnboarding`).
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    /** Records that onboarding was seen. Call before navigating away. */
    fun markSeen() {
        viewModelScope.launch {
            userPreferencesRepository.setHasSeenOnboarding(true)
        }
    }
}
