package pl.tajchert.paczko.fast.core.demo

import pl.tajchert.paczko.fast.core.common.location.LocationProvider
import pl.tajchert.paczko.fast.core.model.collect.GeoPoint
import javax.inject.Inject

/** Fixed fake location (Warsaw centre) so demo collect is device-independent. */
class DemoLocationProvider @Inject constructor() : LocationProvider {
    override suspend fun currentLocation(): GeoPoint =
        GeoPoint(latitude = 52.2297, longitude = 21.0122, accuracy = 5.0)
}
