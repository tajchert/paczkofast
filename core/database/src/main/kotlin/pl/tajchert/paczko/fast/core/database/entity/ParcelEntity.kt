package pl.tajchert.paczko.fast.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parcels")
data class ParcelEntity(
    @PrimaryKey val shipmentNumber: String,
    val status: String,
    val statusGroup: String?,
    val openCode: String?,
    val qrCode: String?,
    val pickupPointName: String?,
    val pickupPointDescription: String?,
    val pickupPointAddress: String?,
    val pickupPointLatitude: Double?,
    val pickupPointLongitude: Double?,
    val expiryDate: String?,
    val storedDate: String?,
    val collectOperation: Boolean,
    val multiCompartmentUuid: String? = null,
    val multiPackageShipmentNumbers: String? = null,
    val ownershipStatus: String? = null,
    val senderName: String? = null,
    val parcelSize: String? = null,
    val pickUpDate: String? = null,
    val returnedToSenderDate: String? = null,
)
