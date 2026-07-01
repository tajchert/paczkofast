package pl.tajchert.paczko.fast.core.database.dao

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import pl.tajchert.paczko.fast.core.database.PaczkofastDatabase
import pl.tajchert.paczko.fast.core.database.entity.ParcelEntity

class ParcelDaoTest {
    private val database = Room.inMemoryDatabaseBuilder(
        InstrumentationRegistry.getInstrumentation().targetContext,
        PaczkofastDatabase::class.java,
    ).build()
    private val dao = database.parcelDao()

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observeParcelsSortsByMostRecentStoredOrExpiryDate() = runBlocking {
        dao.upsertParcels(
            listOf(
                parcelEntity(
                    shipmentNumber = "stored-old",
                    storedDate = "2026-07-01T12:00:00Z",
                    expiryDate = "2026-07-09T12:00:00Z",
                ),
                parcelEntity(
                    shipmentNumber = "expiry-new",
                    storedDate = null,
                    expiryDate = "2026-07-03T12:00:00Z",
                ),
                parcelEntity(
                    shipmentNumber = "stored-new",
                    storedDate = "2026-07-04T12:00:00Z",
                    expiryDate = null,
                ),
            ),
        )

        assertEquals(
            listOf("stored-new", "expiry-new", "stored-old"),
            dao.observeParcels().first().map { it.shipmentNumber },
        )
    }
}

private fun parcelEntity(
    shipmentNumber: String,
    storedDate: String?,
    expiryDate: String?,
) = ParcelEntity(
    shipmentNumber = shipmentNumber,
    status = "ready_to_pickup",
    statusGroup = "ready",
    openCode = "123456",
    qrCode = "qr-$shipmentNumber",
    pickupPointName = null,
    pickupPointDescription = null,
    pickupPointAddress = null,
    pickupPointLatitude = null,
    pickupPointLongitude = null,
    expiryDate = expiryDate,
    storedDate = storedDate,
    collectOperation = true,
    mobileCollectPossible = true,
)
