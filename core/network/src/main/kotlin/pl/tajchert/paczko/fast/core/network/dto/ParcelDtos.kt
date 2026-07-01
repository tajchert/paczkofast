package pl.tajchert.paczko.fast.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class TrackedParcelsResponseDto(
    val parcels: List<ParcelDto>,
    val removedParcelList: List<String> = emptyList(),
    val more: Boolean,
)

@Serializable
data class ParcelDto(
    val shipmentNumber: String,
    val shipmentType: String? = null,
    val status: String,
    val statusGroup: String? = null,
    val openCode: String? = null,
    val qrCode: String? = null,
    val expiryDate: String? = null,
    val storedDate: String? = null,
    val pickUpPoint: PickupPointDto? = null,
    val multiCompartment: MultiCompartmentDto? = null,
    val operations: ParcelOperationsDto = ParcelOperationsDto(),
    val mobileCollectPossible: Boolean? = null,
    val ownershipStatus: String? = null,
)

@Serializable
data class ParcelOperationsDto(
    val collect: Boolean = false,
)

@Serializable
data class MultiCompartmentDto(
    val uuid: String? = null,
    val shipmentNumbers: List<String> = emptyList(),
)

@Serializable
data class PickupPointDto(
    val name: String? = null,
    val locationDescription: String? = null,
    val addressDetails: AddressDetailsDto? = null,
    val location: LocationDto? = null,
)

@Serializable
data class AddressDetailsDto(
    val street: String? = null,
    val buildingNumber: String? = null,
    val city: String? = null,
    val postCode: String? = null,
)

@Serializable
data class LocationDto(
    val latitude: Double? = null,
    val longitude: Double? = null,
)
