package pl.tajchert.paczko.fast.feature.parcels.impl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.ParcelOperations

class PickupWaitLabelTest {

    @Test
    fun formatsWaitInHoursWithinTwoDays() {
        val parcel = parcel(
            storedDate = "2026-07-01T12:00:00Z",
            pickUpDate = "2026-07-03T08:00:00Z", // 44 h later
        )
        assertEquals("44 h", parcel.pickupWaitLabel())
    }

    @Test
    fun formatsWaitInDaysAndHoursBeyondTwoDays() {
        val parcel = parcel(
            storedDate = "2026-07-01T12:00:00Z",
            pickUpDate = "2026-07-04T08:00:00Z", // 2 d 20 h later
        )
        assertEquals("2 d 20 h", parcel.pickupWaitLabel())
    }

    @Test
    fun formatsWaitInMinutesUnderAnHour() {
        val parcel = parcel(
            storedDate = "2026-07-01T12:00:00Z",
            pickUpDate = "2026-07-01T12:35:00Z", // 35 min
        )
        assertEquals("35 min", parcel.pickupWaitLabel())
    }

    @Test
    fun dropsHoursSuffixWhenWholeDays() {
        val parcel = parcel(
            storedDate = "2026-07-01T12:00:00Z",
            pickUpDate = "2026-07-04T12:00:00Z", // exactly 3 d
        )
        assertEquals("3 d", parcel.pickupWaitLabel())
    }

    @Test
    fun nullWhenNotYetCollected() {
        assertNull(parcel(storedDate = "2026-07-01T12:00:00Z", pickUpDate = null).pickupWaitLabel())
    }

    @Test
    fun nullWhenNeverReadyForPickup() {
        assertNull(parcel(storedDate = null, pickUpDate = "2026-07-03T08:00:00Z").pickupWaitLabel())
    }

    @Test
    fun nullWhenTimestampsInconsistent() {
        val parcel = parcel(
            storedDate = "2026-07-03T08:00:00Z",
            pickUpDate = "2026-07-01T12:00:00Z", // collected before ready
        )
        assertNull(parcel.pickupWaitLabel())
    }
}

private fun parcel(storedDate: String?, pickUpDate: String?) = Parcel(
    shipmentNumber = "000000000000",
    status = "delivered",
    statusGroup = "delivered",
    openCode = null,
    qrCode = null,
    pickupPoint = null,
    expiryDate = null,
    storedDate = storedDate,
    operations = ParcelOperations(collect = false),
    pickUpDate = pickUpDate,
)
