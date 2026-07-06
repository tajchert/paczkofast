package pl.tajchert.paczko.fast.core.demo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pl.tajchert.paczko.fast.core.data.repository.ParcelRepository
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.ParcelDetails
import javax.inject.Inject

/** Emits the static demo catalog; refresh is a no-op so fixtures never change. */
class DemoParcelRepository @Inject constructor() : ParcelRepository {
    override fun observeParcels(): Flow<List<Parcel>> = flowOf(DemoData.parcels)

    override fun observeParcel(shipmentNumber: String): Flow<Parcel?> =
        flowOf(DemoData.parcels.firstOrNull { it.shipmentNumber == shipmentNumber })

    override suspend fun refreshTrackedParcels() = Unit

    override fun observeParcelDetails(shipmentNumber: String): Flow<ParcelDetails> =
        flowOf(DemoData.detailsFor(shipmentNumber))

    override suspend fun refreshParcelDetails(shipmentNumber: String) = Unit
}
