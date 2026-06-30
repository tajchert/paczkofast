package pl.tajchert.paczko.fast.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import pl.tajchert.paczko.fast.core.model.DarkThemeConfig
import pl.tajchert.paczko.fast.core.model.TaskSortOrder
import pl.tajchert.paczko.fast.core.model.ThemeBrand
import pl.tajchert.paczko.fast.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
 *     suspend fun setDarkTheme(config: DarkThemeConfig) {
 *         dataSource.setDarkThemeConfig(config)
 *     }
 * }
 * ```
 */
class UserPreferencesDataSource @Inject constructor(
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
        UserPreferences(
            themeBrand = preferences[THEME_BRAND]
                ?.let { ThemeBrand.valueOf(it) }
                ?: ThemeBrand.DEFAULT,
            darkThemeConfig = preferences[DARK_THEME_CONFIG]
                ?.let { DarkThemeConfig.valueOf(it) }
                ?: DarkThemeConfig.FOLLOW_SYSTEM,
            sortOrder = preferences[SORT_ORDER]
                ?.let { TaskSortOrder.valueOf(it) }
                ?: TaskSortOrder.CREATED_DATE,
        )
    }

    /**
     * Set the theme brand preference.
     *
     * ## Transaction Safety
     *
     * The `edit {}` block is transactional:
     * - All changes within the block are atomic
     * - If an exception occurs, no changes are applied
     * - Concurrent edits are serialized
     */
    suspend fun setThemeBrand(themeBrand: ThemeBrand) {
        dataStore.edit { preferences ->
            preferences[THEME_BRAND] = themeBrand.name
        }
    }

    /**
     * Set the dark theme configuration.
     */
    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        dataStore.edit { preferences ->
            preferences[DARK_THEME_CONFIG] = darkThemeConfig.name
        }
    }

    /**
     * Set the task sort order.
     */
    suspend fun setSortOrder(sortOrder: TaskSortOrder) {
        dataStore.edit { preferences ->
            preferences[SORT_ORDER] = sortOrder.name
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
        private val THEME_BRAND = stringPreferencesKey("theme_brand")
        private val DARK_THEME_CONFIG = stringPreferencesKey("dark_theme_config")
        private val SORT_ORDER = stringPreferencesKey("sort_order")
    }
}
