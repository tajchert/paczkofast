package pl.tajchert.paczko.fast.core.data.mapper

import pl.tajchert.paczko.fast.core.network.dto.EventLogEntryDto
import pl.tajchert.paczko.fast.core.network.dto.ParcelDto
import pl.tajchert.paczko.fast.core.network.dto.ParcelOperationsDto
import kotlin.test.Test
import kotlin.test.assertEquals

class TrackingEventMappersTest {

    private fun parcelDto(eventLog: List<EventLogEntryDto>) = ParcelDto(
        shipmentNumber = "123",
        status = "DELIVERED",
        operations = ParcelOperationsDto(collect = false),
        eventLog = eventLog,
    )

    @Test
    fun mapsParcelStatusEntriesPreservingOrder() {
        val dto = parcelDto(
            listOf(
                EventLogEntryDto(type = "PARCEL_STATUS", name = "DELIVERED", date = "2026-05-26T13:00:13.328Z"),
                EventLogEntryDto(type = "PARCEL_STATUS", name = "OUT_FOR_DELIVERY", date = "2026-05-26T05:18:54.780Z"),
            ),
        )

        val events = dto.toTrackingEvents()

        assertEquals(2, events.size)
        assertEquals("DELIVERED", events[0].status)
        assertEquals("2026-05-26T13:00:13.328Z", events[0].date)
        assertEquals("OUT_FOR_DELIVERY", events[1].status)
    }

    @Test
    fun dropsNonStatusAndNamelessEntries() {
        val dto = parcelDto(
            listOf(
                EventLogEntryDto(type = "INVOICE", name = "INVOICE_REQUESTED", date = "2026-05-26T13:00:13.328Z"),
                EventLogEntryDto(type = "PARCEL_STATUS", name = null, date = "2026-05-26T13:00:13.328Z"),
                EventLogEntryDto(type = "PARCEL_STATUS", name = "CONFIRMED", date = null),
            ),
        )

        val events = dto.toTrackingEvents()

        assertEquals(1, events.size)
        assertEquals("CONFIRMED", events[0].status)
        assertEquals(null, events[0].date)
    }

    @Test
    fun emptyEventLogMapsToEmptyList() {
        assertEquals(emptyList(), parcelDto(emptyList()).toTrackingEvents())
    }
}
