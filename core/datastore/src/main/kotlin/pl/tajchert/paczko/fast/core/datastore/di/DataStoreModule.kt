package pl.tajchert.paczko.fast.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing DataStore instances.
 *
 * ## Singleton Scope
 *
 * DataStore must be a singleton because:
 * 1. Multiple instances can corrupt the file
 * 2. DataStore handles concurrent access internally
 * 3. Creating DataStore is relatively expensive
 *
 * ## File Location
 *
 * The preferences file is stored at:
 * `/data/data/<package>/files/datastore/user_preferences.preferences_pb`
 */
@Module
@InstallIn(SingletonComponent::class)
internal object DataStoreModule {

    /**
     * Provides the user preferences DataStore.
     *
     * ## Why PreferenceDataStoreFactory?
     *
     * We use the factory directly instead of the `preferencesDataStore` delegate
     * because Hilt modules can't use property delegates easily.
     * This gives us the same functionality with more control.
     */
    @Provides
    @Singleton
    fun providesUserPreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("user_preferences") },
    )
}
