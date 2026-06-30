package pl.tajchert.paczko.fast.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import pl.tajchert.paczko.fast.core.database.entity.ParcelEntity

@Dao
interface ParcelDao {
    @Query("SELECT * FROM parcels ORDER BY storedDate DESC, shipmentNumber ASC")
    fun observeParcels(): Flow<List<ParcelEntity>>

    @Query("SELECT * FROM parcels WHERE shipmentNumber = :shipmentNumber")
    fun observeParcel(shipmentNumber: String): Flow<ParcelEntity?>

    @Transaction
    suspend fun applyTrackedParcelPage(
        parcels: List<ParcelEntity>,
        removedShipmentNumbers: List<String>,
    ) {
        upsertParcels(parcels)
        if (removedShipmentNumbers.isNotEmpty()) {
            deleteParcels(removedShipmentNumbers)
        }
    }

    @Upsert
    suspend fun upsertParcels(parcels: List<ParcelEntity>)

    @Query("DELETE FROM parcels WHERE shipmentNumber IN (:shipmentNumbers)")
    suspend fun deleteParcels(shipmentNumbers: List<String>)

    @Query("DELETE FROM parcels")
    suspend fun clearParcels()
}
