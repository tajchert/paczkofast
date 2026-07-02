package pl.tajchert.paczko.fast.core.model.parcel

data class ParcelOperations(
    val collect: Boolean,
)

data class Parcel(
    val shipmentNumber: String,
    val status: String,
    val statusGroup: String?,
    val openCode: String?,
    val qrCode: String?,
    val pickupPoint: PickupPoint?,
    val expiryDate: String?,
    val storedDate: String?,
    val operations: ParcelOperations,
    val multiCompartmentUuid: String? = null,
    val multiPackageShipmentNumbers: List<String> = emptyList(),
    val ownershipStatus: String? = null,
    val senderName: String? = null,
    val parcelSize: String? = null,
    val pickUpDate: String? = null,
    val returnedToSenderDate: String? = null,
) {
    val canCollectRemotely: Boolean
        get() = operations.collect && openCode.isNullOrBlank().not()

    val isMultiPackage: Boolean
        get() = multiCompartmentUuid.isNullOrBlank().not() || multiPackageShipmentNumbers.isNotEmpty()

    val isSharedFromSomeone: Boolean
        get() {
            val normalizedStatus = ownershipStatus?.trim()?.uppercase()
            return normalizedStatus != null && normalizedStatus !in OWNER_STATUSES
        }

    private companion object {
        // Observed live values are `OWN` (you own it) and `FRIEND` (shared with
        // you). "OWNER"/"OWNED" are kept as defensive legacy aliases so only a
        // genuinely-shared parcel (e.g. FRIEND) is flagged as shared-from-someone.
        val OWNER_STATUSES = setOf("OWN", "OWNER", "OWNED")
    }
}
