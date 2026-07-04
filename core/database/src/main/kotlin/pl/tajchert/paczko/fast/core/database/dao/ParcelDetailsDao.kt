package pl.tajchert.paczko.fast.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import pl.tajchert.paczko.fast.core.database.entity.ParcelDetailsEntity
import pl.tajchert.paczko.fast.core.database.entity.TrackingEventEntity

@Dao
interface ParcelDetailsDao {
    @Query("SELECT * FROM parcel_details WHERE shipmentNumber = :shipmentNumber")
    fun observeDetails(shipmentNumber: String): Flow<ParcelDetailsEntity?>

    @Query("SELECT * FROM tracking_events WHERE shipmentNumber = :shipmentNumber ORDER BY position ASC")
    fun observeTrackingEvents(shipmentNumber: String): Flow<List<TrackingEventEntity>>

    /** Overwrites the cached details + timeline for one parcel in a single transaction. */
    @Transaction
    suspend fun replaceParcelDetails(
        details: ParcelDetailsEntity,
        events: List<TrackingEventEntity>,
    ) {
        upsertDetails(details)
        deleteTrackingEvents(details.shipmentNumber)
        if (events.isNotEmpty()) {
            upsertTrackingEvents(events)
        }
    }

    @Upsert
    suspend fun upsertDetails(details: ParcelDetailsEntity)

    @Upsert
    suspend fun upsertTrackingEvents(events: List<TrackingEventEntity>)

    @Query("DELETE FROM tracking_events WHERE shipmentNumber = :shipmentNumber")
    suspend fun deleteTrackingEvents(shipmentNumber: String)
}
