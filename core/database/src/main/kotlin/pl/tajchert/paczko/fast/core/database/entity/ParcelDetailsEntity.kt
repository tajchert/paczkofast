package pl.tajchert.paczko.fast.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached scalar detail fields for a parcel (fetched when the parcel is opened),
 * so the detail screen renders offline. The tracking timeline lives separately
 * in [TrackingEventEntity].
 */
@Entity(tableName = "parcel_details")
data class ParcelDetailsEntity(
    @PrimaryKey val shipmentNumber: String,
    val sizeCode: String? = null,
    val senderName: String? = null,
    val shipmentType: String? = null,
)
