package pl.tajchert.paczko.fast.feature.parcels.impl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.ParcelOperations

class MultiPackageGroupingTest {

    private fun parcel(
        shipmentNumber: String,
        multiCompartmentUuid: String? = null,
        multiPackageShipmentNumbers: List<String> = emptyList(),
    ) = Parcel(
        shipmentNumber = shipmentNumber,
        status = "ready_to_pickup",
        statusGroup = "ready",
        openCode = "code-$shipmentNumber",
        qrCode = null,
        pickupPoint = null,
        expiryDate = null,
        storedDate = null,
        operations = ParcelOperations(collect = true),
        multiCompartmentUuid = multiCompartmentUuid,
        multiPackageShipmentNumbers = multiPackageShipmentNumbers,
    )

    @Test
    fun groupsMembersSharingACompartmentIntoOneMultiItem() {
        val a = parcel("111", multiCompartmentUuid = "mc-1", multiPackageShipmentNumbers = listOf("111", "222"))
        val b = parcel("222", multiCompartmentUuid = "mc-1")
        val solo = parcel("333")

        val items = groupByCompartment(listOf(a, b, solo))

        assertEquals(2, items.size)
        val multi = items[0] as CompartmentItem.Multi
        assertEquals("mc-1", multi.group.uuid)
        assertEquals(listOf("111", "222"), multi.group.members.map { it.shipmentNumber })
        // Representative is the member carrying the full shipment-number list.
        assertEquals("111", multi.group.representative.shipmentNumber)
        assertEquals("333", (items[1] as CompartmentItem.Single).parcel.shipmentNumber)
    }

    @Test
    fun loneCompartmentMemberStaysSingle() {
        val lone = parcel("111", multiCompartmentUuid = "mc-1")

        val items = groupByCompartment(listOf(lone))

        assertEquals(1, items.size)
        assertTrue(items[0] is CompartmentItem.Single)
    }

    @Test
    fun groupTakesPositionOfItsFirstMember() {
        val solo = parcel("000")
        val a = parcel("111", multiCompartmentUuid = "mc-1")
        val b = parcel("222", multiCompartmentUuid = "mc-1")

        val items = groupByCompartment(listOf(solo, a, b))

        assertEquals("000", (items[0] as CompartmentItem.Single).parcel.shipmentNumber)
        assertTrue(items[1] is CompartmentItem.Multi)
        assertEquals(2, items.size)
    }
}
