package pl.tajchert.paczko.fast.core.common.location

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface LocationModule {
    @Binds
    fun bindsLocationProvider(provider: AndroidLocationProvider): LocationProvider
}
