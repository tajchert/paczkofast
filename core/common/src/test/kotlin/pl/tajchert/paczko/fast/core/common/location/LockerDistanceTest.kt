package pl.tajchert.paczko.fast.core.common.location

import org.junit.Assert.assertEquals
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
}
