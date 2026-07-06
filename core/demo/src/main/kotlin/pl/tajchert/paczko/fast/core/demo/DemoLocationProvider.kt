package pl.tajchert.paczko.fast.core.demo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pl.tajchert.paczko.fast.core.common.location.LocationProvider
import pl.tajchert.paczko.fast.core.model.collect.GeoPoint
import javax.inject.Inject

/** Fixed fake location (Warsaw centre) so demo collect is device-independent. */
class DemoLocationProvider @Inject constructor() : LocationProvider {
    override suspend fun currentLocation(): GeoPoint = DEMO_LOCATION

    // Emit the same fixed fake fix so Nearby/live-distance UI is deterministic in demo.
    override fun locationUpdates(): Flow<GeoPoint> = flowOf(DEMO_LOCATION)

    private companion object {
        val DEMO_LOCATION = GeoPoint(latitude = 52.2297, longitude = 21.0122, accuracy = 5.0)
    }
}
