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
    val mobileCollectPossible: Boolean?,
) {
    val canCollectRemotely: Boolean
        get() = operations.collect && mobileCollectPossible == true && openCode.isNullOrBlank().not()
}
