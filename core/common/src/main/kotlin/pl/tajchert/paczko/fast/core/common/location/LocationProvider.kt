package pl.tajchert.paczko.fast.core.common.location

import kotlinx.coroutines.flow.Flow
import pl.tajchert.paczko.fast.core.model.collect.GeoPoint

interface LocationProvider {
    /** One authoritative fix (used for collect validation). */
    suspend fun currentLocation(): GeoPoint

    /**
     * Stream of location fixes: emits a fast initial value (last known) when available,
     * then refines as better fixes arrive. Completes without emitting if permission is
     * missing. Callers cancel by cancelling collection.
     */
    fun locationUpdates(): Flow<GeoPoint>
}
