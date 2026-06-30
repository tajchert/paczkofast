package pl.tajchert.paczko.fast.core.database.di

import android.content.Context
import androidx.room.Room
import pl.tajchert.paczko.fast.core.database.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
 * ## Build Patterns
 *
 * For development, you might want to add:
 * - `.fallbackToDestructiveMigration()` for easier schema iteration
 * - `.setQueryCallback()` for SQL logging
 *
 * For production, ensure proper migrations are in place.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    /**
     * Provides the singleton Room database instance.
     */
    @Provides
    @Singleton
    fun providesTaskDatabase(
        @ApplicationContext context: Context,
    ): TaskDatabase = Room.databaseBuilder(
        context,
        TaskDatabase::class.java,
        "paczkofast-database",
    )
        // For development: destroy and recreate on schema changes
        // Remove this for production and add proper migrations
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
}
