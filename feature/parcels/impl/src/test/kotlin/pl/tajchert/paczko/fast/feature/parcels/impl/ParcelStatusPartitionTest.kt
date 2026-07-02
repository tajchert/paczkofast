package pl.tajchert.paczko.fast.feature.parcels.impl

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.ParcelOperations

class ParcelStatusPartitionTest {

    private fun parcel(status: String, statusGroup: String? = null) = Parcel(
        shipmentNumber = "123",
        status = status,
        statusGroup = statusGroup,
        openCode = null,
        qrCode = null,
        pickupPoint = null,
        expiryDate = null,
        storedDate = null,
        operations = ParcelOperations(collect = false),
    )

    @Test
    fun finishedStatusesAreHistory() {
        listOf(
            parcel("DELIVERED", "DELIVERED"),
            parcel("collected_by_customer"),
            parcel("collected_from_sender"),
            parcel("returned_to_sender"),
            parcel("pickup_time_expired"),
            parcel("undelivered_wrong_address"),
            parcel("canceled"),
        ).forEach { assertTrue("expected finished: ${it.status}", it.isFinished) }
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
