package pl.tajchert.paczko.fast.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import pl.tajchert.paczko.fast.core.database.dao.ParcelDao
import pl.tajchert.paczko.fast.core.database.dao.ParcelDetailsDao
import pl.tajchert.paczko.fast.core.database.entity.ParcelDetailsEntity
import pl.tajchert.paczko.fast.core.database.entity.ParcelEntity
import pl.tajchert.paczko.fast.core.database.entity.TrackingEventEntity
import pl.tajchert.paczko.fast.core.network.InpostParcelApi
import pl.tajchert.paczko.fast.core.network.dto.EventLogEntryDto
import pl.tajchert.paczko.fast.core.network.dto.MultiCompartmentDto
import pl.tajchert.paczko.fast.core.network.dto.ParcelDto
import pl.tajchert.paczko.fast.core.network.dto.ParcelOperationsDto
import pl.tajchert.paczko.fast.core.network.dto.TrackedParcelsResponseDto
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultParcelRepositoryTest {

    @Test
    fun refreshTrackedParcelsAccumulatesPagesUntilMoreIsFalse() = runTest {
        val network = FakeParcelApi(
            TrackedParcelsResponseDto(parcels = listOf(parcelDto("1")), more = true),
            TrackedParcelsResponseDto(parcels = listOf(parcelDto("2")), more = false),
        )
        val dao = FakeParcelDao()
        val repository = DefaultParcelRepository(network, dao, FakeParcelDetailsDao())

        repository.refreshTrackedParcels()

        assertEquals(listOf("1", "2"), dao.saved.map { it.shipmentNumber })
        assertEquals(2, dao.appliedPages)
        assertEquals(2, network.calls)
    }

    @Test
    fun refreshTrackedParcelsRemovesDeletedShipmentNumbers() = runTest {
        val network = FakeParcelApi(
            TrackedParcelsResponseDto(
                parcels = emptyList(),
                removedParcelList = listOf("old"),
                more = false,
            ),
        )
        val dao = FakeParcelDao(existing = listOf(parcelEntity("old")))
        val repository = DefaultParcelRepository(network, dao, FakeParcelDetailsDao())

        repository.refreshTrackedParcels()

        assertEquals(emptyList(), dao.saved.map { it.shipmentNumber })
        assertEquals(1, dao.appliedPages)
    }

    @Test
    fun refreshTrackedParcelsPersistsMultiPackageAndOwnershipMetadata() = runTest {
        val network = FakeParcelApi(
            TrackedParcelsResponseDto(
                parcels = listOf(
                    parcelDto("multi-shared").copy(
                        multiCompartment = MultiCompartmentDto(
                            uuid = "multi-uuid",
                            shipmentNumbers = listOf("multi-shared", "sibling"),
                        ),
                        ownershipStatus = "SHARED_TO_ME",
                    ),
                ),
                more = false,
            ),
        )
        val dao = FakeParcelDao()
        val repository = DefaultParcelRepository(network, dao, FakeParcelDetailsDao())

        repository.refreshTrackedParcels()

        assertEquals("multi-uuid", dao.saved.single().multiCompartmentUuid)
        assertEquals("multi-shared,sibling", dao.saved.single().multiPackageShipmentNumbers)
        assertEquals("SHARED_TO_ME", dao.saved.single().ownershipStatus)
    }

    @Test
    fun refreshParcelDetailsCachesEventsAndDisplayFields() = runTest {
        val network = FakeParcelApi(
            detail = parcelDto("123").copy(
                parcelSize = "C",
                sender = pl.tajchert.paczko.fast.core.network.dto.SenderDto(name = "Amazon Polska"),
                shipmentType = "courier",
                eventLog = listOf(
                    EventLogEntryDto(type = "PARCEL_STATUS", name = "DELIVERED", date = "2026-05-26T13:00:13.328Z"),
                    EventLogEntryDto(type = "INVOICE", name = "INVOICE_REQUESTED", date = "2026-05-26T13:00:13.328Z"),
                ),
            ),
        )
        val repository = DefaultParcelRepository(network, FakeParcelDao(), FakeParcelDetailsDao())

        repository.refreshParcelDetails("123")
        val details = repository.observeParcelDetails("123").first()

        assertEquals(listOf("DELIVERED"), details.events.map { it.status })
        assertEquals("C", details.sizeCode)
        assertEquals("Amazon Polska", details.senderName)
        assertEquals("courier", details.shipmentType)
    }

    @Test
    fun observeParcelDetailsEmitsEmptyWhenNothingCached() = runTest {
        val repository = DefaultParcelRepository(FakeParcelApi(), FakeParcelDao(), FakeParcelDetailsDao())

        val details = repository.observeParcelDetails("nope").first()

        assertEquals(emptyList(), details.events)
        assertEquals(null, details.sizeCode)
    }
}

private fun parcelDto(number: String) = ParcelDto(
    shipmentNumber = number,
    status = "ready_to_pickup",
    statusGroup = "ready",
    openCode = "123456",
    qrCode = "qr-$number",
    operations = ParcelOperationsDto(collect = true),
)

private fun parcelEntity(number: String) = ParcelEntity(
    shipmentNumber = number,
    status = "ready_to_pickup",
    statusGroup = "ready",
    openCode = "123456",
    qrCode = "qr-$number",
    pickupPointName = null,
    pickupPointDescription = null,
    pickupPointAddress = null,
    pickupPointLatitude = null,
    pickupPointLongitude = null,
    expiryDate = null,
    storedDate = null,
    collectOperation = true,
)

private class FakeParcelApi(
    private vararg val responses: TrackedParcelsResponseDto,
    private val detail: ParcelDto? = null,
) : InpostParcelApi {
    var calls = 0
        private set

    override suspend fun getTrackedParcels(): TrackedParcelsResponseDto =
        responses[calls++]

    override suspend fun getTrackedParcel(shipmentNumber: String): ParcelDto =
        detail ?: error("Unexpected getTrackedParcel call")
}

private class FakeParcelDetailsDao : ParcelDetailsDao {
    private val details = MutableStateFlow<Map<String, ParcelDetailsEntity>>(emptyMap())
    private val events = MutableStateFlow<Map<String, List<TrackingEventEntity>>>(emptyMap())

    override fun observeDetails(shipmentNumber: String): Flow<ParcelDetailsEntity?> =
        details.map { it[shipmentNumber] }

    override fun observeTrackingEvents(shipmentNumber: String): Flow<List<TrackingEventEntity>> =
        events.map { it[shipmentNumber].orEmpty().sortedBy(TrackingEventEntity::position) }

    override suspend fun upsertDetails(details: ParcelDetailsEntity) {
        this.details.value = this.details.value + (details.shipmentNumber to details)
    }

    override suspend fun upsertTrackingEvents(events: List<TrackingEventEntity>) {
        val shipmentNumber = events.firstOrNull()?.shipmentNumber ?: return
        this.events.value = this.events.value + (shipmentNumber to events)
    }

    override suspend fun deleteTrackingEvents(shipmentNumber: String) {
        events.value = events.value - shipmentNumber
    }
}

private class FakeParcelDao(
    existing: List<ParcelEntity> = emptyList(),
) : ParcelDao {
    private val parcels = MutableStateFlow(existing)

    val saved: List<ParcelEntity>
        get() = parcels.value

    var appliedPages = 0
        private set

    override fun observeParcels(): Flow<List<ParcelEntity>> = parcels

    override fun observeParcel(shipmentNumber: String): Flow<ParcelEntity?> =
        MutableStateFlow(parcels.value.firstOrNull { it.shipmentNumber == shipmentNumber })

    override suspend fun applyTrackedParcelPage(
        parcels: List<ParcelEntity>,
        removedShipmentNumbers: List<String>,
    ) {
        appliedPages++
        upsertPageParcels(parcels)
        deletePageParcels(removedShipmentNumbers)
    }

    override suspend fun upsertParcels(parcels: List<ParcelEntity>) {
        error("refreshTrackedParcels should use applyTrackedParcelPage")
    }

    override suspend fun deleteParcels(shipmentNumbers: List<String>) {
        error("refreshTrackedParcels should use applyTrackedParcelPage")
    }

    override suspend fun clearParcels() {
        parcels.value = emptyList()
    }

    private fun upsertPageParcels(parcels: List<ParcelEntity>) {
        val incomingByNumber = parcels.associateBy { it.shipmentNumber }
        this.parcels.value = this.parcels.value
            .filterNot { it.shipmentNumber in incomingByNumber.keys } + parcels
    }

    private fun deletePageParcels(shipmentNumbers: List<String>) {
        parcels.value = parcels.value.filterNot { it.shipmentNumber in shipmentNumbers }
    }
}
