package com.demo.sample.core.data.repository

import com.demo.sample.core.datastore.UserPreferencesDataSource
import com.demo.sample.core.model.DarkThemeConfig
import com.demo.sample.core.model.TaskSortOrder
import com.demo.sample.core.model.ThemeBrand
import com.demo.sample.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Repository for user preferences.
 *
 * ## Architecture
 *
 * Unlike [TaskRepository], this doesn't follow offline-first because
 * user preferences are purely local - they never sync to a server.
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

    /**
     * Set the theme brand.
     */
    suspend fun setThemeBrand(themeBrand: ThemeBrand)

    /**
     * Set the dark theme configuration.
     */
    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)

    /**
     * Set the task sort order.
     */
    suspend fun setSortOrder(sortOrder: TaskSortOrder)
}

/**
 * Default implementation backed by Proto DataStore.
 */
class DefaultUserPreferencesRepository @Inject constructor(
    private val dataSource: UserPreferencesDataSource,
) : UserPreferencesRepository {

    override val userPreferences: Flow<UserPreferences> = dataSource.userPreferences

    override suspend fun setThemeBrand(themeBrand: ThemeBrand) {
        dataSource.setThemeBrand(themeBrand)
    }

    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        dataSource.setDarkThemeConfig(darkThemeConfig)
    }

    override suspend fun setSortOrder(sortOrder: TaskSortOrder) {
        dataSource.setSortOrder(sortOrder)
    }
}
