package pl.tajchert.paczko.fast.core.demo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pl.tajchert.paczko.fast.core.common.location.LocationProvider
import pl.tajchert.paczko.fast.core.model.collect.GeoPoint
import javax.inject.Inject

/**
 * Fixed fake location standing a few metres from the demo locker (see
 * DemoData's pickup point), so the collect screen shows a small, sensible
 * distance ("~11 m") and Nearby mode reads as "at the locker" — deterministic
 * and device-independent.
 */
class DemoLocationProvider @Inject constructor() : LocationProvider {
    override suspend fun currentLocation(): GeoPoint = DEMO_LOCATION

    // Emit the same fixed fake fix so Nearby/live-distance UI is deterministic in demo.
    override fun locationUpdates(): Flow<GeoPoint> = flowOf(DEMO_LOCATION)

    private companion object {
        // ~11 m north of the demo locker at (52.2402, 20.9319).
        val DEMO_LOCATION = GeoPoint(latitude = 52.2403, longitude = 20.9319, accuracy = 8.0)
    }
}
