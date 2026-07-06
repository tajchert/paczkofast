package pl.tajchert.paczko.fast.core.demo

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DemoLocationProviderTest {
    @Test
    fun `currentLocation returns a fixed fake point`() = runTest {
        val point = DemoLocationProvider().currentLocation()
        assertEquals(52.2297, point.latitude, 0.0001)
        assertEquals(21.0122, point.longitude, 0.0001)
    }
}
