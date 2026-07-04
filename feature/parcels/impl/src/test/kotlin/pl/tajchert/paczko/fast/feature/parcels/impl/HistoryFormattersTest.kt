package pl.tajchert.paczko.fast.feature.parcels.impl

import org.junit.Assert.assertEquals
import org.junit.Test
import pl.tajchert.paczko.fast.core.designsystem.component.HistoryOutcome
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.ParcelOperations
import pl.tajchert.paczko.fast.core.model.parcel.PickupPoint
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

class HistoryFormattersTest {

    private val zone = ZoneId.of("Europe/Warsaw")
    private val now = Instant.parse("2026-07-03T09:00:00Z")

    private fun parcel(
        status: String,
        storedDate: String? = null,
        pickUpDate: String? = null,
        lockerName: String? = null,
    ) = Parcel(
        shipmentNumber = "123",
        status = status,
        statusGroup = null,
        openCode = null,
        qrCode = null,
        pickupPoint = lockerName?.let {
            PickupPoint(name = it, locationDescription = null, addressLine = null, latitude = null, longitude = null)
        },
        expiryDate = null,
        storedDate = storedDate,
        operations = ParcelOperations(collect = false),
        pickUpDate = pickUpDate,
    )

    @Test
    fun outcomeClassification() {
        assertEquals(HistoryOutcome.PickedUp, historyOutcome(parcel("claimed")))
        assertEquals(HistoryOutcome.PickedUp, historyOutcome(parcel("delivered")))
        assertEquals(HistoryOutcome.Expired, historyOutcome(parcel("pickup_time_expired")))
        assertEquals(HistoryOutcome.Returned, historyOutcome(parcel("returned_to_sender")))
        assertEquals(HistoryOutcome.Returned, historyOutcome(parcel("canceled")))
    }

    @Test
    fun outcomeLineIncludesLockerForPickedUp() {
        assertEquals(
            "Picked up · Locker WAW01A",
            historyOutcomeLine(parcel("claimed", lockerName = "WAW01A")),
        )
        assertEquals("Picked up", historyOutcomeLine(parcel("claimed")))
        assertEquals("Expired · returned to sender", historyOutcomeLine(parcel("pickup_time_expired")))
        assertEquals("Returned to sender", historyOutcomeLine(parcel("returned_to_sender")))
    }

    @Test
    fun dateLabelShowsTimeInCurrentMonthAndDateOnlyForOlder() {
        val thisMonth = parcel("claimed", storedDate = "2026-07-02T12:32:00Z")
        val lastMonth = parcel("claimed", storedDate = "2026-06-28T12:00:00Z")

        assertEquals("2 Jul, 14:32", historyDateLabel(thisMonth, now = now, zone = zone))
        assertEquals("28 Jun", historyDateLabel(lastMonth, now = now, zone = zone))
        assertEquals("", historyDateLabel(parcel("claimed"), now = now, zone = zone))
    }

    @Test
    fun dateLabelPrefersPickUpDateOverStoredDate() {
        // pickUpDate is the real collection time and must win over storedDate.
        val p = parcel(
            "claimed",
            storedDate = "2026-06-20T08:00:00Z",
            pickUpDate = "2026-07-02T12:32:00Z",
        )
        assertEquals("2 Jul, 14:32", historyDateLabel(p, now = now, zone = zone))
    }

    @Test
    fun monthLabelAddsYearOnlyForOtherYears() {
        assertEquals("July", historyMonthLabel(YearMonth.of(2026, 7), now = now, zone = zone))
        assertEquals("June 2025", historyMonthLabel(YearMonth.of(2025, 6), now = now, zone = zone))
    }
}
