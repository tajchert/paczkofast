package pl.tajchert.paczko.fast.core.data.mapper

import pl.tajchert.paczko.fast.core.database.entity.ParcelEntity
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.ParcelOperations
import pl.tajchert.paczko.fast.core.model.parcel.PickupPoint
import pl.tajchert.paczko.fast.core.network.dto.ParcelDto

fun ParcelDto.toEntity() = ParcelEntity(
    shipmentNumber = shipmentNumber,
    status = status,
    statusGroup = statusGroup,
    openCode = openCode,
    qrCode = qrCode,
    pickupPointName = pickUpPoint?.name,
    pickupPointDescription = pickUpPoint?.locationDescription,
    pickupPointAddress = pickUpPoint?.addressDetails?.let {
        listOfNotNull(it.street, it.buildingNumber, it.postCode, it.city).joinToString(" ")
    },
    pickupPointLatitude = pickUpPoint?.location?.latitude,
    pickupPointLongitude = pickUpPoint?.location?.longitude,
    expiryDate = expiryDate,
    storedDate = storedDate,
    collectOperation = operations.collect,
    multiCompartmentUuid = multiCompartment?.uuid,
    multiPackageShipmentNumbers = multiCompartment?.shipmentNumbers.toStoredShipmentNumbers(),
    ownershipStatus = ownershipStatus,
)

fun ParcelEntity.toDomain() = Parcel(
    shipmentNumber = shipmentNumber,
    status = status,
    statusGroup = statusGroup,
    openCode = openCode,
    qrCode = qrCode,
    pickupPoint = pickupPointName?.let {
        PickupPoint(
            name = it,
            locationDescription = pickupPointDescription,
            addressLine = pickupPointAddress,
            latitude = pickupPointLatitude,
            longitude = pickupPointLongitude,
        )
    },
    expiryDate = expiryDate,
    storedDate = storedDate,
    operations = ParcelOperations(collect = collectOperation),
    multiCompartmentUuid = multiCompartmentUuid,
    multiPackageShipmentNumbers = multiPackageShipmentNumbers.toShipmentNumberList(),
    ownershipStatus = ownershipStatus,
)

private fun List<String>?.toStoredShipmentNumbers(): String? =
    this
        ?.map(String::trim)
        ?.filter(String::isNotEmpty)
        ?.joinToString(",")
        ?.takeIf(String::isNotBlank)

private fun String?.toShipmentNumberList(): List<String> =
    this
        ?.split(",")
        ?.map(String::trim)
        ?.filter(String::isNotEmpty)
        .orEmpty()
