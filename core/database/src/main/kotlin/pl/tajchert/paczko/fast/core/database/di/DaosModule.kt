package pl.tajchert.paczko.fast.core.database.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.tajchert.paczko.fast.core.database.PaczkofastDatabase
import pl.tajchert.paczko.fast.core.database.dao.ParcelDao
import pl.tajchert.paczko.fast.core.database.dao.ParcelDetailsDao

/**
 * Hilt module providing DAO instances.
 *
 * ## Why a Separate Module?
 *
 * We separate DAO provision from database provision because:
 * 1. DAOs are the public API of the database layer
 * 2. Consumers inject DAOs, not the database directly
 * 3. This makes it easier to swap implementations for testing
 *
 * ## Scope
 *
 * DAOs are not scoped (no @Singleton) because:
 * - They're lightweight interfaces
 * - The underlying database is already singleton
 * - Room handles connection pooling internally
 */
@Module
@InstallIn(SingletonComponent::class)
internal object DaosModule {

    @Provides
    fun providesParcelDao(database: PaczkofastDatabase): ParcelDao = database.parcelDao()

    @Provides
    fun providesParcelDetailsDao(database: PaczkofastDatabase): ParcelDetailsDao =
        database.parcelDetailsDao()
}
