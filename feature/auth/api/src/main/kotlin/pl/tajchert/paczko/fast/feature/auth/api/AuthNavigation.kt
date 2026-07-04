package pl.tajchert.paczko.fast.feature.auth.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object AuthRoute : NavKey

/**
 * First-launch welcome + disclaimer flow, shown before [AuthRoute] until the
 * user has completed it once (see `UserPreferences.hasSeenOnboarding`).
 */
@Serializable
data object OnboardingRoute : NavKey
