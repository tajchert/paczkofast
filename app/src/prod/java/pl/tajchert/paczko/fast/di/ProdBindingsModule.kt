package pl.tajchert.paczko.fast.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.tajchert.paczko.fast.core.common.location.AndroidLocationProvider
import pl.tajchert.paczko.fast.core.common.location.LocationProvider
import pl.tajchert.paczko.fast.core.data.repository.AuthRepository
import pl.tajchert.paczko.fast.core.data.repository.CollectRepository
import pl.tajchert.paczko.fast.core.data.repository.DefaultAuthRepository
import pl.tajchert.paczko.fast.core.data.repository.DefaultCollectRepository
import pl.tajchert.paczko.fast.core.data.repository.DefaultParcelRepository
import pl.tajchert.paczko.fast.core.data.repository.ParcelRepository
import javax.inject.Singleton

/** Real data/location bindings for the production flavor. */
@Module
@InstallIn(SingletonComponent::class)
abstract class ProdBindingsModule {

    @Binds
    @Singleton
    abstract fun bindsParcelRepository(impl: DefaultParcelRepository): ParcelRepository

    @Binds
    @Singleton
    abstract fun bindsCollectRepository(impl: DefaultCollectRepository): CollectRepository

    @Binds
    @Singleton
    abstract fun bindsAuthRepository(impl: DefaultAuthRepository): AuthRepository

    @Binds
    abstract fun bindsLocationProvider(impl: AndroidLocationProvider): LocationProvider
}
