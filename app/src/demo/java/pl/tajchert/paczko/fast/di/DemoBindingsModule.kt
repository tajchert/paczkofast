package pl.tajchert.paczko.fast.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.tajchert.paczko.fast.core.common.location.LocationProvider
import pl.tajchert.paczko.fast.core.data.repository.AuthRepository
import pl.tajchert.paczko.fast.core.data.repository.CollectRepository
import pl.tajchert.paczko.fast.core.data.repository.ParcelRepository
import pl.tajchert.paczko.fast.core.demo.DemoAuthRepository
import pl.tajchert.paczko.fast.core.demo.DemoCollectRepository
import pl.tajchert.paczko.fast.core.demo.DemoLocationProvider
import pl.tajchert.paczko.fast.core.demo.DemoParcelRepository
import javax.inject.Singleton

/** Offline fakes for the demo flavor — never reaches the network. */
@Module
@InstallIn(SingletonComponent::class)
abstract class DemoBindingsModule {

    @Binds
    @Singleton
    abstract fun bindsParcelRepository(impl: DemoParcelRepository): ParcelRepository

    @Binds
    @Singleton
    abstract fun bindsCollectRepository(impl: DemoCollectRepository): CollectRepository

    @Binds
    @Singleton
    abstract fun bindsAuthRepository(impl: DemoAuthRepository): AuthRepository

    @Binds
    abstract fun bindsLocationProvider(impl: DemoLocationProvider): LocationProvider
}
