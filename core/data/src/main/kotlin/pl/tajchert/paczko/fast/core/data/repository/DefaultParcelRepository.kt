package pl.tajchert.paczko.fast.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pl.tajchert.paczko.fast.core.data.mapper.toDomain
import pl.tajchert.paczko.fast.core.data.mapper.toEntity
import pl.tajchert.paczko.fast.core.database.dao.ParcelDao
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.network.InpostParcelApi
import javax.inject.Inject

class DefaultParcelRepository @Inject constructor(
    private val api: InpostParcelApi,
    private val parcelDao: ParcelDao,
) : ParcelRepository {
    override fun observeParcels(): Flow<List<Parcel>> =
        parcelDao.observeParcels().map { entities -> entities.map { it.toDomain() } }

    override fun observeParcel(shipmentNumber: String): Flow<Parcel?> =
        parcelDao.observeParcel(shipmentNumber).map { it?.toDomain() }

    override suspend fun refreshTrackedParcels() {
        do {
            val response = api.getTrackedParcels()
            parcelDao.applyTrackedParcelPage(
                parcels = response.parcels.map { it.toEntity() },
                removedShipmentNumbers = response.removedParcelList,
            )
        } while (response.more)
    }
}
