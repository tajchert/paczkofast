package pl.tajchert.paczko.fast.core.data.repository

import pl.tajchert.paczko.fast.core.datastore.UserPreferencesDataSource
import pl.tajchert.paczko.fast.core.model.LockerOpenMode
import pl.tajchert.paczko.fast.core.model.ParcelListOpenButtonMode
import pl.tajchert.paczko.fast.core.model.ThemeMode
import pl.tajchert.paczko.fast.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Repository for user preferences.
 *
 * ## Architecture
 *
 * User preferences are purely local - they never sync to a server.
 *
 * The default implementation is a thin wrapper around
 * [UserPreferencesDataSource] that provides a cleaner API to consumers.
 *
 * ## Why a Repository?
 *
 * Even for local-only data, the repository pattern provides:
 * 1. Abstraction from storage implementation (DataStore)
 * 2. Single point of access for preferences
 * 3. Consistent API with other repositories
 * 4. Easy to fake for testing (see FakeUserPreferencesRepository)
 */
interface UserPreferencesRepository {

    /**
     * Flow of current user preferences.
     */
    val userPreferences: Flow<UserPreferences>

    suspend fun setThemeMode(themeMode: ThemeMode)

    suspend fun setHasSeenOnboarding(seen: Boolean)

    suspend fun setLockerOpenMode(mode: LockerOpenMode)

    suspend fun setParcelListOpenButtonMode(mode: ParcelListOpenButtonMode)
}

/**
 * Default implementation backed by Proto DataStore.
 */
class DefaultUserPreferencesRepository @Inject constructor(
    private val dataSource: UserPreferencesDataSource,
) : UserPreferencesRepository {

    override val userPreferences: Flow<UserPreferences> = dataSource.userPreferences

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        dataSource.setThemeMode(themeMode)
    }

    override suspend fun setHasSeenOnboarding(seen: Boolean) {
        dataSource.setHasSeenOnboarding(seen)
    }

    override suspend fun setLockerOpenMode(mode: LockerOpenMode) {
        dataSource.setLockerOpenMode(mode)
    }

    override suspend fun setParcelListOpenButtonMode(mode: ParcelListOpenButtonMode) {
        dataSource.setParcelListOpenButtonMode(mode)
    }
}
