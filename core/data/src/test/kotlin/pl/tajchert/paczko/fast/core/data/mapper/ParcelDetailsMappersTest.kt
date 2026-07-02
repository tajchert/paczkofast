package pl.tajchert.paczko.fast.core.data.mapper

import pl.tajchert.paczko.fast.core.network.dto.EventLogEntryDto
import pl.tajchert.paczko.fast.core.network.dto.ParcelDto
import pl.tajchert.paczko.fast.core.network.dto.ParcelOperationsDto
import pl.tajchert.paczko.fast.core.network.dto.SenderDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ParcelDetailsMappersTest {

    private fun dto(
        parcelSize: String? = null,
        sender: SenderDto? = null,
        shipmentType: String? = null,
        eventLog: List<EventLogEntryDto> = emptyList(),
    ) = ParcelDto(
        shipmentNumber = "123",
        status = "DELIVERED",
        shipmentType = shipmentType,
        parcelSize = parcelSize,
        sender = sender,
        operations = ParcelOperationsDto(collect = false),
        eventLog = eventLog,
    )

    @Test
    fun mapsAllDetailFields() {
        val details = dto(
            parcelSize = "C",
            sender = SenderDto(name = "Amazon Polska"),
            shipmentType = "courier",
            eventLog = listOf(
                EventLogEntryDto(type = "PARCEL_STATUS", name = "DELIVERED", date = "2026-05-26T13:00:13.328Z"),
            ),
        ).toParcelDetails()

        assertEquals("C", details.sizeCode)
        assertEquals("Amazon Polska", details.senderName)
        assertEquals("courier", details.shipmentType)
        assertEquals(listOf("DELIVERED"), details.events.map { it.status })
    }

    @Test
    fun nullSenderYieldsNullSenderName() {
        val details = dto(sender = null).toParcelDetails()
        assertNull(details.senderName)
    }

    @Test
    fun emptyDtoYieldsEmptyDefaults() {
        val details = dto().toParcelDetails()
        assertNull(details.sizeCode)
        assertNull(details.senderName)
        assertNull(details.shipmentType)
        assertEquals(emptyList(), details.events)
    }
}
