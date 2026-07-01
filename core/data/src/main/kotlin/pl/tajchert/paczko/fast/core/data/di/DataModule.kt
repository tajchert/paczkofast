package pl.tajchert.paczko.fast.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.tajchert.paczko.fast.core.data.auth.DataStoreTokenProvider
import pl.tajchert.paczko.fast.core.data.repository.AuthRepository
import pl.tajchert.paczko.fast.core.data.repository.CollectRepository
import pl.tajchert.paczko.fast.core.data.repository.DefaultAuthRepository
import pl.tajchert.paczko.fast.core.data.repository.DefaultCollectRepository
import pl.tajchert.paczko.fast.core.data.repository.DefaultParcelRepository
import pl.tajchert.paczko.fast.core.data.repository.DefaultUserPreferencesRepository
import pl.tajchert.paczko.fast.core.data.repository.ParcelRepository
import pl.tajchert.paczko.fast.core.data.repository.UserPreferencesRepository
import pl.tajchert.paczko.fast.core.network.auth.TokenProvider
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

    @Binds
    @Singleton
    internal abstract fun bindsAuthRepository(
        impl: DefaultAuthRepository,
    ): AuthRepository

    @Binds
    @Singleton
    internal abstract fun bindsParcelRepository(
        impl: DefaultParcelRepository,
    ): ParcelRepository

    @Binds
    @Singleton
    internal abstract fun bindsCollectRepository(
        impl: DefaultCollectRepository,
    ): CollectRepository

    @Binds
    @Singleton
    internal abstract fun bindsTokenProvider(
        impl: DataStoreTokenProvider,
    ): TokenProvider

    /**
     * Binds the default implementation of UserPreferencesRepository.
     */
    @Binds
    @Singleton
    internal abstract fun bindsUserPreferencesRepository(
        impl: DefaultUserPreferencesRepository,
    ): UserPreferencesRepository
}
