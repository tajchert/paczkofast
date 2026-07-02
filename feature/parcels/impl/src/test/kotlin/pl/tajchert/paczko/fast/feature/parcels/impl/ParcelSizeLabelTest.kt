package pl.tajchert.paczko.fast.feature.parcels.impl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ParcelSizeLabelTest {

    @Test
    fun mapsKnownCodesToLabels() {
        assertEquals("XS", parcelSizeLabel("D"))
        listOf("A", "E", "H").forEach { assertEquals("S", parcelSizeLabel(it)) }
        listOf("B", "F", "I").forEach { assertEquals("M", parcelSizeLabel(it)) }
        listOf("C", "G", "J").forEach { assertEquals("L", parcelSizeLabel(it)) }
    }

    @Test
    fun isCaseInsensitive() {
        assertEquals("L", parcelSizeLabel("c"))
    }

    @Test
    fun unknownOrNullYieldsNull() {
        assertNull(parcelSizeLabel(null))
        assertNull(parcelSizeLabel("OTHER"))
        assertNull(parcelSizeLabel("Z"))
        assertNull(parcelSizeLabel(""))
    }
}
