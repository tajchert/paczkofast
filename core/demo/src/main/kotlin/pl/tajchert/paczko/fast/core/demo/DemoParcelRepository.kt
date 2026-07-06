package pl.tajchert.paczko.fast.core.demo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pl.tajchert.paczko.fast.core.data.repository.ParcelRepository
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.ParcelDetails
import javax.inject.Inject

class DemoParcelRepository @Inject constructor() : ParcelRepository {
    override fun observeParcels(): Flow<List<Parcel>> = flowOf(emptyList())
    override fun observeParcel(shipmentNumber: String): Flow<Parcel?> = flowOf(null)
    override suspend fun refreshTrackedParcels() = Unit
    override fun observeParcelDetails(shipmentNumber: String): Flow<ParcelDetails> = flowOf(ParcelDetails())
    override suspend fun refreshParcelDetails(shipmentNumber: String) = Unit
}
