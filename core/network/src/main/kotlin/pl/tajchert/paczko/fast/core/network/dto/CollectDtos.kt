package pl.tajchert.paczko.fast.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CollectValidateRequestDto(
    val parcel: ParcelCompartmentDto,
    val geoPoint: GeoPointDto,
)

@Serializable
data class ParcelCompartmentDto(
    val shipmentNumber: String,
    val openCode: String,
)

@Serializable
data class GeoPointDto(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double,
)

@Serializable
data class CollectValidateResponseDto(
    val sessionUuid: String,
)

@Serializable
data class CollectSessionRequestDto(
    val sessionUuid: String,
)

@Serializable
data class CollectStatusRequestDto(
    val sessionUuid: String,
    val expectedStatus: String,
)

@Serializable
data class CollectClaimRequestDto(
    val sessionUuid: String,
    val shipmentNumbers: List<String>,
)

@Serializable
data class CompartmentResponseDto(
    val status: String? = null,
    val openCompartmentWaitingTime: Long? = null,
    val actionTime: Long? = null,
    val confirmActionTime: Long? = null,
)
