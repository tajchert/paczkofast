package pl.tajchert.paczko.fast.core.model.parcel

data class PickupPoint(
    val name: String,
    val locationDescription: String?,
    val addressLine: String?,
    val latitude: Double?,
    val longitude: Double?,
)
