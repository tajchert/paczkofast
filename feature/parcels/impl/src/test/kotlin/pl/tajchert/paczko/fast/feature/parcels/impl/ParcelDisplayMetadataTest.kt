package pl.tajchert.paczko.fast.feature.parcels.impl

import org.junit.Assert.assertEquals
import org.junit.Test
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.ParcelOperations

class ParcelDisplayMetadataTest {
    @Test
    fun metadataLinesDescribeMultiPackageAndSharedParcel() {
        val parcel = parcel(
            multiCompartmentUuid = "multi-uuid",
            multiPackageShipmentNumbers = listOf("123", "456"),
            ownershipStatus = "SHARED_TO_ME",
        )

        assertEquals(
            listOf("Multi-package: 123, 456", "Shared package"),
            parcelMetadataLines(parcel),
        )
    }

    @Test
    fun metadataLinesDescribeMultiPackageWithoutShipmentNumbers() {
        val parcel = parcel(multiCompartmentUuid = "multi-uuid")

        assertEquals(listOf("Multi-package"), parcelMetadataLines(parcel))
    }

    @Test
    fun metadataLinesAreEmptyForOwnedSinglePackage() {
        val parcel = parcel(ownershipStatus = "OWNER")

        assertEquals(emptyList<String>(), parcelMetadataLines(parcel))
    }
}

private fun parcel(
    multiCompartmentUuid: String? = null,
    multiPackageShipmentNumbers: List<String> = emptyList(),
    ownershipStatus: String? = null,
) = Parcel(
    shipmentNumber = "123",
    status = "ready_to_pickup",
    statusGroup = "ready",
    openCode = "123456",
    qrCode = "opaque-qr",
    pickupPoint = null,
    expiryDate = null,
    storedDate = null,
    operations = ParcelOperations(collect = true),
    multiCompartmentUuid = multiCompartmentUuid,
    multiPackageShipmentNumbers = multiPackageShipmentNumbers,
    ownershipStatus = ownershipStatus,
)
