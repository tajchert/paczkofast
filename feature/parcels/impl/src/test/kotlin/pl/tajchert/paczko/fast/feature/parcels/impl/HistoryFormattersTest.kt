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
            "Odebrano · Paczkomat WAW01A",
            historyOutcomeLine(parcel("claimed", lockerName = "WAW01A")),
        )
        assertEquals("Odebrano", historyOutcomeLine(parcel("claimed")))
        assertEquals("Termin minął · zwrot do nadawcy", historyOutcomeLine(parcel("pickup_time_expired")))
        assertEquals("Zwrócona do nadawcy", historyOutcomeLine(parcel("returned_to_sender")))
    }

    @Test
    fun dateLabelShowsDateOnlyWithoutTime() {
        val thisMonth = parcel("claimed", storedDate = "2026-07-02T12:32:00Z")
        val lastMonth = parcel("claimed", storedDate = "2026-06-28T12:00:00Z")

        assertEquals("2 lip", historyDateLabel(thisMonth, zone = zone))
        assertEquals("28 cze", historyDateLabel(lastMonth, zone = zone))
        assertEquals("", historyDateLabel(parcel("claimed"), zone = zone))
    }

    @Test
    fun dateLabelPrefersPickUpDateOverStoredDate() {
        // pickUpDate is the real collection date and must win over storedDate.
        val p = parcel(
            "claimed",
            storedDate = "2026-06-20T08:00:00Z",
            pickUpDate = "2026-07-02T12:32:00Z",
        )
        assertEquals("2 lip", historyDateLabel(p, zone = zone))
    }

    @Test
    fun monthLabelAddsYearOnlyForOtherYears() {
        assertEquals("lipca", historyMonthLabel(YearMonth.of(2026, 7), now = now, zone = zone))
        assertEquals("czerwca 2025", historyMonthLabel(YearMonth.of(2025, 6), now = now, zone = zone))
    }
}
