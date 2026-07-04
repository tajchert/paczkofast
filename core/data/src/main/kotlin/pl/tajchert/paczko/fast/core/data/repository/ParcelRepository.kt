package pl.tajchert.paczko.fast.core.data.repository

import kotlinx.coroutines.flow.Flow
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.ParcelDetails

interface ParcelRepository {
    fun observeParcels(): Flow<List<Parcel>>
    fun observeParcel(shipmentNumber: String): Flow<Parcel?>
    suspend fun refreshTrackedParcels()

    /** The cached detail fields + tracking timeline for a parcel (survives offline cold start). */
    fun observeParcelDetails(shipmentNumber: String): Flow<ParcelDetails>

    /** Fetches the latest parcel detail from the network and caches it. */
    suspend fun refreshParcelDetails(shipmentNumber: String)
}
