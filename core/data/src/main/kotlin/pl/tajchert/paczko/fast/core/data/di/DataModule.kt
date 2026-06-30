package pl.tajchert.paczko.fast.core.data.di

import pl.tajchert.paczko.fast.core.data.repository.DefaultTaskRepository
import pl.tajchert.paczko.fast.core.data.repository.DefaultUserPreferencesRepository
import pl.tajchert.paczko.fast.core.data.repository.TaskRepository
import pl.tajchert.paczko.fast.core.data.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for data layer bindings.
 *
 * ## Why @Binds?
 *
 * We use @Binds instead of @Provides for interface bindings because:
 * 1. More efficient - Dagger generates less code
 * 2. Clearer intent - shows we're binding an impl to an interface
 * 3. Compile-time verification that impl implements the interface
 *
 * ## Scope
 *
 * Repository is @Singleton because:
 * - It holds no state (stateless operations)
 * - Creating multiple instances wastes memory
 * - All callers should see the same data
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    /**
     * Binds the default implementation of TaskRepository.
     *
     * Consumers inject TaskRepository interface, not DefaultTaskRepository.
     * This enables swapping implementations for testing.
     */
    @Binds
    @Singleton
    internal abstract fun bindsTaskRepository(
        impl: DefaultTaskRepository,
    ): TaskRepository

    /**
     * Binds the default implementation of UserPreferencesRepository.
     */
    @Binds
    @Singleton
    internal abstract fun bindsUserPreferencesRepository(
        impl: DefaultUserPreferencesRepository,
    ): UserPreferencesRepository
}
