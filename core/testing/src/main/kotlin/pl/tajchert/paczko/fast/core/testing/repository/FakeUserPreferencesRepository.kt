package pl.tajchert.paczko.fast.core.testing.repository

import pl.tajchert.paczko.fast.core.data.repository.UserPreferencesRepository
import pl.tajchert.paczko.fast.core.model.LockerOpenMode
import pl.tajchert.paczko.fast.core.model.ParcelListOpenButtonMode
import pl.tajchert.paczko.fast.core.model.ThemeMode
import pl.tajchert.paczko.fast.core.model.UserPreferences
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Fake implementation of [UserPreferencesRepository] for testing.
 */
class FakeUserPreferencesRepository : UserPreferencesRepository {

    private val preferencesFlow = MutableSharedFlow<UserPreferences>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /**
     * Current preferences (for test verification).
     */
    val currentPreferences: UserPreferences
        get() = preferencesFlow.replayCache.firstOrNull() ?: UserPreferences()

    init {
        // Initialize with defaults
        preferencesFlow.tryEmit(UserPreferences())
    }

    // =========================================================================
    // Test Control Methods
    // =========================================================================

    /**
     * Set the preferences to return.
     */
    fun setPreferences(preferences: UserPreferences) {
        preferencesFlow.tryEmit(preferences)
    }

    // =========================================================================
    // Repository Implementation
    // =========================================================================

    override val userPreferences: Flow<UserPreferences> = preferencesFlow

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        val current = currentPreferences
        preferencesFlow.tryEmit(current.copy(themeMode = themeMode))
    }

    override suspend fun setHasSeenOnboarding(seen: Boolean) {
        val current = currentPreferences
        preferencesFlow.tryEmit(current.copy(hasSeenOnboarding = seen))
    }

    override suspend fun setLockerOpenMode(mode: LockerOpenMode) {
        val current = currentPreferences
        preferencesFlow.tryEmit(current.copy(lockerOpenMode = mode))
    }

    override suspend fun setParcelListOpenButtonMode(mode: ParcelListOpenButtonMode) {
        val current = currentPreferences
        preferencesFlow.tryEmit(current.copy(parcelListOpenButtonMode = mode))
    }
}
