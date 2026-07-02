package pl.tajchert.paczko.fast.feature.parcels.impl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.ParcelOperations

class ParcelStatusPartitionTest {

    private fun parcel(
        status: String,
        statusGroup: String? = null,
        expiryDate: String? = null,
        storedDate: String? = null,
        shipmentNumber: String = "123",
    ) = Parcel(
        shipmentNumber = shipmentNumber,
        status = status,
        statusGroup = statusGroup,
        openCode = null,
        qrCode = null,
        pickupPoint = null,
        expiryDate = expiryDate,
        storedDate = storedDate,
        operations = ParcelOperations(collect = false),
    )

    @Test
    fun historySortKeyOrdersNewestFirstWithUndatedLast() {
        val newer = parcel("claimed", storedDate = "2026-07-01T10:00:00Z", shipmentNumber = "new")
        val older = parcel("claimed", storedDate = "2026-06-01T10:00:00Z", shipmentNumber = "old")
        val undated = parcel("claimed", shipmentNumber = "undated")

        val sorted = listOf(older, undated, newer)
            .sortedByDescending { it.historySortKey() }
            .map { it.shipmentNumber }

        assertEquals(listOf("new", "old", "undated"), sorted)
    }

    @Test
    fun finishedStatusesAreHistory() {
        listOf(
            parcel("DELIVERED", "DELIVERED"),
            parcel("claimed"),
            parcel("collected_by_customer"),
            parcel("collected_from_sender"),
            parcel("returned_to_sender"),
            parcel("pickup_time_expired"),
            parcel("undelivered_wrong_address"),
            parcel("canceled"),
        ).forEach { assertTrue("expected finished: ${it.status}", it.isFinished) }
    }

    @Test
    fun pickedUpStatusesAreCollected() {
        listOf(
            parcel("claimed"),
            parcel("CLAIMED"),
            parcel("collected_by_customer"),
            parcel("collected_from_sender"),
            parcel("DELIVERED", "DELIVERED"),
        ).forEach { assertTrue("expected picked up: ${it.status}", it.isPickedUp) }
    }

    @Test
    fun nonPickedUpStatusesAreNotCollected() {
        listOf(
            parcel("READY_TO_PICKUP", "TO_PICKUP"),
            parcel("out_for_delivery"),
            parcel("canceled"),
            parcel("pickup_time_expired"),
        ).forEach { assertFalse("expected not picked up: ${it.status}", it.isPickedUp) }
    }

    @Test
    fun activeAndUnknownStatusesAreNotHistory() {
        listOf(
            parcel("READY_TO_PICKUP", "TO_PICKUP"),
            parcel("out_for_delivery"),
            parcel("created"),
            parcel("some_future_status"),
        ).forEach { assertFalse("expected active: ${it.status}", it.isFinished) }
    }
}
