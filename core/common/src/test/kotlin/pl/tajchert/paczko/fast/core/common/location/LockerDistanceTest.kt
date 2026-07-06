package pl.tajchert.paczko.fast.core.common.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.tajchert.paczko.fast.core.model.collect.GeoPoint

class LockerDistanceTest {

    private fun point(lat: Double, lng: Double) = GeoPoint(lat, lng, accuracy = 5.0)

    @Test
    fun samePointIsZeroMeters() {
        val here = point(50.061, 19.938)
        assertEquals(0, metersToLocker(here, 50.061, 19.938))
    }

    @Test
    fun nullLatitudeReturnsNull() {
        assertNull(metersToLocker(point(50.061, 19.938), null, 19.938))
    }

    @Test
    fun nullLongitudeReturnsNull() {
        assertNull(metersToLocker(point(50.061, 19.938), 50.061, null))
    }

    @Test
    fun oneThousandthDegreeLatitudeIsAboutOneHundredElevenMeters() {
        // 0.001 degrees of latitude is ~111 m anywhere on Earth.
        val distance = metersToLocker(point(50.061, 19.938), 50.062, 19.938)!!
        assertTrue("expected ~111 m, was $distance", distance in 105..117)
    }

    @Test
    fun withinThresholdWhenCloseAndAccurate() {
        assertTrue(isWithinNearbyThreshold(distanceMeters = 10, accuracyMeters = 8))
        assertTrue(isWithinNearbyThreshold(distanceMeters = 49, accuracyMeters = 30))
    }

    @Test
    fun notWithinThresholdAtOrBeyondDistanceLimit() {
        assertFalse(isWithinNearbyThreshold(distanceMeters = 50, accuracyMeters = 5))
        assertFalse(isWithinNearbyThreshold(distanceMeters = 51, accuracyMeters = 5))
    }

    @Test
    fun notWithinThresholdWhenFixTooCoarse() {
        assertFalse(isWithinNearbyThreshold(distanceMeters = 10, accuracyMeters = 31))
    }

    @Test
    fun notWithinThresholdWhenDataMissing() {
        assertFalse(isWithinNearbyThreshold(distanceMeters = null, accuracyMeters = 5))
        assertFalse(isWithinNearbyThreshold(distanceMeters = 10, accuracyMeters = null))
    }

    @Test
    fun constantsMatchSpec() {
        assertEquals(50, NEARBY_DISTANCE_METERS)
        assertEquals(30, NEARBY_ACCURACY_METERS)
    }
}
