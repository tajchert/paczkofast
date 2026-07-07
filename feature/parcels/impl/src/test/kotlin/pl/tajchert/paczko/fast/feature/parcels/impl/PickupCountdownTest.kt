package pl.tajchert.paczko.fast.feature.parcels.impl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.ParcelOperations
import java.time.Instant
import java.util.Locale

class PickupCountdownTest {

    @Before
    fun setLocale() {
        Locale.setDefault(Locale.ENGLISH)
    }

    @Test
    fun formatsNonUrgentCountdownWithTimeLeftSuffix() {
        val countdown = pickupCountdown(
            parcel(
                expiryDate = "2026-07-03T10:00:00Z",
                storedDate = "2026-07-01T10:00:00Z",
            ),
            now = Instant.parse("2026-07-01T12:00:00Z"),
        )

        requireNotNull(countdown)
        assertEquals("46 h left", countdown.timeLeftText)
        assertEquals("46 h", countdown.countdownText)
        assertEquals("46h", countdown.compactTimeLeftText)
        assertFalse(countdown.urgent)
    }

    @Test
    fun formatsLastHoursCountdownAsHurry() {
        val countdown = pickupCountdown(
            parcel(
                expiryDate = "2026-07-01T21:00:00Z",
                storedDate = "2026-06-29T21:00:00Z",
            ),
            now = Instant.parse("2026-07-01T12:00:00Z"),
        )

        requireNotNull(countdown)
        assertEquals("9 h — hurry!", countdown.timeLeftText)
        assertEquals("9 h — hurry!", countdown.countdownText)
        assertEquals("9h!", countdown.compactTimeLeftText)
        assertTrue(countdown.urgent)
    }

    @Test
    fun treatsPickupReminderStatusAsHurry() {
        val countdown = pickupCountdown(
            parcel(
                status = "pickup_reminder_sent",
                expiryDate = "2026-07-03T10:00:00Z",
                storedDate = "2026-07-01T10:00:00Z",
            ),
            now = Instant.parse("2026-07-01T12:00:00Z"),
        )

        requireNotNull(countdown)
        assertEquals("46 h — hurry!", countdown.timeLeftText)
        assertEquals("46 h — hurry!", countdown.countdownText)
        assertEquals("46h!", countdown.compactTimeLeftText)
        assertTrue(countdown.urgent)
    }

    @Test
    fun formatsPolishCountdownWhenLocaleIsPolish() {
        Locale.setDefault(Locale.forLanguageTag("pl-PL"))

        val countdown = pickupCountdown(
            parcel(
                expiryDate = "2026-07-03T10:00:00Z",
                storedDate = "2026-07-01T10:00:00Z",
            ),
            now = Instant.parse("2026-07-01T12:00:00Z"),
        )

        requireNotNull(countdown)
        assertEquals("zostało 46 h", countdown.timeLeftText)
    }
}

private fun parcel(
    status: String = "ready_to_pickup",
    expiryDate: String,
    storedDate: String?,
) = Parcel(
    shipmentNumber = "000000000000",
    status = status,
    statusGroup = "ready",
    openCode = "123456",
    qrCode = null,
    pickupPoint = null,
    expiryDate = expiryDate,
    storedDate = storedDate,
    operations = ParcelOperations(collect = true),
)
