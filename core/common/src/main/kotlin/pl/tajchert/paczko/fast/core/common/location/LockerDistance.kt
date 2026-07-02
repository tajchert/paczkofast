package pl.tajchert.paczko.fast.core.common.location

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import pl.tajchert.paczko.fast.core.model.collect.GeoPoint

private const val EARTH_RADIUS_METERS = 6_371_000.0

/**
 * Great-circle distance in whole metres between the user's [from] location and
 * a locker at [lockerLatitude] / [lockerLongitude]. Returns null when either
 * locker coordinate is missing.
 */
fun metersToLocker(
    from: GeoPoint,
    lockerLatitude: Double?,
    lockerLongitude: Double?,
): Int? {
    val lat = lockerLatitude ?: return null
    val lng = lockerLongitude ?: return null
    val dLat = Math.toRadians(lat - from.latitude)
    val dLng = Math.toRadians(lng - from.longitude)
    val a = sin(dLat / 2).pow(2) +
        cos(Math.toRadians(from.latitude)) * cos(Math.toRadians(lat)) *
        sin(dLng / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return (EARTH_RADIUS_METERS * c).roundToInt()
}
