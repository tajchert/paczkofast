package pl.tajchert.paczko.fast.core.database.entity

import androidx.room.Entity

/**
 * One cached tracking-history row for a parcel. [position] preserves the
 * API's newest-first ordering so the timeline renders identically offline.
 */
@Entity(tableName = "tracking_events", primaryKeys = ["shipmentNumber", "position"])
data class TrackingEventEntity(
    val shipmentNumber: String,
    val position: Int,
    val status: String,
    val date: String? = null,
)
