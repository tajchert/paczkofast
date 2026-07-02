package pl.tajchert.paczko.fast.core.data.repository

import kotlinx.coroutines.flow.Flow
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.TrackingEvent

interface ParcelRepository {
    fun observeParcels(): Flow<List<Parcel>>
    fun observeParcel(shipmentNumber: String): Flow<Parcel?>
    suspend fun refreshTrackedParcels()
    suspend fun getTrackingEvents(shipmentNumber: String): List<TrackingEvent>
}
