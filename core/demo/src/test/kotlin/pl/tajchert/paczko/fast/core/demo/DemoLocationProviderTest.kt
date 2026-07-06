package pl.tajchert.paczko.fast.core.demo

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DemoLocationProviderTest {
    @Test
    fun `currentLocation returns a fixed fake point by the demo locker`() = runTest {
        val point = DemoLocationProvider().currentLocation()
        assertEquals(52.2403, point.latitude, 0.0001)
        assertEquals(20.9319, point.longitude, 0.0001)
    }
}
