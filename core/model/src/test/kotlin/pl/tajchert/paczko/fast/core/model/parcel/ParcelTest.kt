package pl.tajchert.paczko.fast.core.model.parcel

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ParcelTest {
    @Test
    fun collectableParcelRequiresCollectOperationAndMobileCollectFlag() {
        val parcel = Parcel(
            shipmentNumber = "123",
            status = "ready_to_pickup",
            statusGroup = "ready",
            openCode = "123456",
            qrCode = "opaque-qr",
            pickupPoint = null,
            expiryDate = null,
            storedDate = null,
            operations = ParcelOperations(collect = true),
            mobileCollectPossible = true,
        )

        assertTrue(parcel.canCollectRemotely)
        assertFalse(parcel.copy(mobileCollectPossible = false).canCollectRemotely)
        assertFalse(parcel.copy(operations = ParcelOperations(collect = false)).canCollectRemotely)
        assertFalse(parcel.copy(openCode = "").canCollectRemotely)
        assertFalse(parcel.copy(openCode = null).canCollectRemotely)
    }

    @Test
    fun parcelFlagsIdentifyMultiPackageAndSharedFromSomeone() {
        val parcel = Parcel(
            shipmentNumber = "123",
            status = "ready_to_pickup",
            statusGroup = "ready",
            openCode = "123456",
            qrCode = "opaque-qr",
            pickupPoint = null,
            expiryDate = null,
            storedDate = null,
            operations = ParcelOperations(collect = true),
            mobileCollectPossible = true,
            multiCompartmentUuid = "multi-uuid",
            multiPackageShipmentNumbers = listOf("123", "456"),
            ownershipStatus = "SHARED_TO_ME",
        )

        assertTrue(parcel.isMultiPackage)
        assertTrue(parcel.isSharedFromSomeone)
        assertFalse(parcel.copy(multiCompartmentUuid = null, multiPackageShipmentNumbers = emptyList()).isMultiPackage)
        assertFalse(parcel.copy(ownershipStatus = "OWNER").isSharedFromSomeone)
        assertFalse(parcel.copy(ownershipStatus = null).isSharedFromSomeone)
    }
}
