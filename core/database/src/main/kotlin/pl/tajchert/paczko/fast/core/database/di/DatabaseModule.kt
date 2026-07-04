package pl.tajchert.paczko.fast.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.tajchert.paczko.fast.core.database.PaczkofastDatabase
import pl.tajchert.paczko.fast.core.database.migration.MIGRATIONS
import javax.inject.Singleton

/**
 * Hilt module providing the Room database instance.
 *
 * ## Singleton Scope
 *
 * The database is scoped as [Singleton] because:
 * 1. Room database creation is expensive
 * 2. All DAOs should share the same database connection
 * 3. Multiple instances would cause data inconsistencies
 *
 * ## Migrations
 *
 * Schema changes are handled by explicit [MIGRATIONS] that preserve the offline
 * cache. See `core/database/schemas/…` for the exported schema per version.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    /**
     * Provides the singleton Room database instance.
     */
    @Provides
    @Singleton
    fun providesPaczkofastDatabase(
        @ApplicationContext context: Context,
    ): PaczkofastDatabase = Room.databaseBuilder(
        context,
        PaczkofastDatabase::class.java,
        "paczkofast-database",
    )
        .addMigrations(*MIGRATIONS)
        .build()
}
