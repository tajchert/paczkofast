package pl.tajchert.paczko.fast.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pl.tajchert.paczko.fast.core.datastore.di.UserPreferencesDataStore
import pl.tajchert.paczko.fast.core.model.ThemeMode
import pl.tajchert.paczko.fast.core.model.UserPreferences
import javax.inject.Inject

/**
 * DataSource for user preferences backed by Jetpack DataStore.
 *
 * ## Architecture Role
 *
 * This class is the single access point for user preferences.
 * It sits in the data layer and:
 * - Exposes preferences as a Flow for reactive updates
 * - Provides suspend functions for modifying preferences
 * - Handles serialization/deserialization of enum values
 *
 * ## Why DataStore?
 *
 * Compared to SharedPreferences:
 * 1. **Async by default**: All reads are non-blocking
 * 2. **Flow-based**: UI automatically updates when preferences change
 * 3. **Transactional**: Uses `edit {}` for atomic updates
 * 4. **Type-safe**: Typed keys prevent wrong-type access
 *
 * ## Usage
 *
 * ```kotlin
 * // In Repository
 * class UserPreferencesRepository @Inject constructor(
 *     private val dataSource: UserPreferencesDataSource,
 * ) {
 *     val userPreferences: Flow<UserPreferences> = dataSource.userPreferences
 *
 *     suspend fun setThemeMode(themeMode: ThemeMode) {
 *         dataSource.setThemeMode(themeMode)
 *     }
 * }
 * ```
 */
class UserPreferencesDataSource @Inject constructor(
    @UserPreferencesDataStore
    private val dataStore: DataStore<Preferences>,
) {
    /**
     * Flow of current user preferences.
     *
     * This Flow:
     * - Emits immediately with current preferences
     * - Re-emits whenever any preference changes
     * - Never completes (it's a hot flow)
     */
    val userPreferences: Flow<UserPreferences> = dataStore.data.map { preferences ->
        val stored = preferences[THEME_MODE]
        val themeMode = stored
            ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.SYSTEM
        UserPreferences(themeMode = themeMode)
    }

    /**
     * Set the theme mode preference.
     *
     * ## Transaction Safety
     *
     * The `edit {}` block is transactional:
     * - All changes within the block are atomic
     * - If an exception occurs, no changes are applied
     * - Concurrent edits are serialized
     */
    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = themeMode.name
        }
    }

    companion object {
        /**
         * Preference keys.
         *
         * ## Naming Convention
         *
         * We use descriptive string keys that match the property names.
         * This makes debugging easier when inspecting DataStore files.
         */
        private val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
