package pl.tajchert.paczko.fast.core.common.location

import pl.tajchert.paczko.fast.core.model.collect.GeoPoint

interface LocationProvider {
    suspend fun currentLocation(): GeoPoint
}
