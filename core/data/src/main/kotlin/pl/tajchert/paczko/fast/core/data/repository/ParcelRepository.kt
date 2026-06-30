package pl.tajchert.paczko.fast.core.data.repository

import kotlinx.coroutines.flow.Flow
import pl.tajchert.paczko.fast.core.model.parcel.Parcel

interface ParcelRepository {
    fun observeParcels(): Flow<List<Parcel>>
    fun observeParcel(shipmentNumber: String): Flow<Parcel?>
    suspend fun refreshTrackedParcels()
}
